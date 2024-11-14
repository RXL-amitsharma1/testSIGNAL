import pyAgrum as gum
import queue
import numpy as np
from django.conf import settings
import logging
import os
import sys

default_error_message = "Inside dss_network.py - An error occurred on line {}. Exception type: {}, Exception: {} "


class NetworkUtility:
    def __init__(self):
        pass

    @staticmethod
    def get_parents_dict(model, non_terminal_nodes, id_to_name):
        """
        return a dict of format {'node': parent_node_list, ...}
        """
        try:
            parents_for_non_terminal = {}
            for node in non_terminal_nodes:
                parents_for_non_terminal[node] = [id_to_name[p_id] for p_id in model.parents(node)]
            return parents_for_non_terminal
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    @staticmethod
    def get_deactivated_cpd(node_name, model):
        """
        return CPD table object with all probabilities as 0.5
        """
        try:
            variables_in_cpt = model.cpt(node_name).var_names
            shape = []
            for _var in variables_in_cpt:
                node_states = model.variable(_var).labels()
                shape.append(len(node_states))
            return np.full(shape, 0.5)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    @staticmethod
    def add_deactivate_cpd_to_model(deactivated_nodes, model):
        """
        Adds CPD with all 0.5 probability values to the model
        """
        try:
            for node in deactivated_nodes:
                model.cpt(node)[:] = NetworkUtility.get_deactivated_cpd(node, model)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    @staticmethod
    def get_deactivated_nodes(model, remove_nodes, non_terminal_nodes, id_to_name):
        """
        Given a list of nodes we want to deactivate
        Assumption if a terminal node is left with
        no input node then that node should also be considered deactivated.

        return: a list of nodes which will be deactivated automatically.
        """
        try:
            q = queue.Queue()
            parents_for_non_terminal = NetworkUtility.get_parents_dict(model, non_terminal_nodes, id_to_name)
            visited = {}

            # populate queue
            for node in remove_nodes:
                q.put(node)

            while not q.empty():
                node = q.get()

                if node in visited:
                    continue
                visited[node] = True

                # considering children of the current nodes
                children = [id_to_name[c_id] for c_id in model.children(node)]
                if len(children) > 0:
                    children = children[0]
                    if node in parents_for_non_terminal[children]:
                        parents_for_non_terminal[children].remove(node)
                    if len(parents_for_non_terminal[children]) == 0:
                        remove_nodes.append(children)
                        q.put(children)

                # considering parent of given node if node is non-terminal
                if node in parents_for_non_terminal:
                    for parent in parents_for_non_terminal[node]:
                        remove_nodes.append(parent)
                        q.put(parent)

            return list(set(remove_nodes))
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


class Model:
    network_util = NetworkUtility()

    def __init__(self, dss_config, remove_nodes=[], concept_file=None, print_logs=True):
        try:
            self.model = None
            self.network = None
            self.terminal_nodes = None
            self.non_terminal_nodes = None
            self.deactivated_nodes = None
            self.state_dict = None
            self.name_to_id = None
            self.id_to_name = None
            self.edges = None
            self.print_logs = print_logs

            if self.print_logs:
                logging.info(f"Remove nodes: {remove_nodes}")

            # make sure all nodes are node names not the node label
            _remove_nodes = []
            for node in remove_nodes:
                if node in dss_config.label_node_dict:
                    _remove_nodes.append(dss_config.label_node_dict[node])
                elif node in dss_config.node_label_dict:
                    _remove_nodes.append(node)

            if self.print_logs:
                logging.info(f"Remapped Remove nodes: {_remove_nodes}")

            # verify if remove_nodes have invalid node names
            if len(_remove_nodes) < len(remove_nodes):
                raise Exception("Invalid node name is defined for DEACTIVATED_NODES parameter in config.ini")

            self.initialize_network(Model.network_util, _remove_nodes, concept_file)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def initialize_network(self, network_util, remove_nodes, concept_file):
        try:
            if self.print_logs:
                logging.info('Application configuration: Importing network from concept file and object creation '
                             'for VariableElimination algorithm.')
            if concept_file is None:
                concept_file = os.path.join(settings.CONFIG_FILES, 'concept.bif')
                # pass
            self.model = gum.loadBN(concept_file)
            self.name_to_id = {name: self.model.idFromName(name) for name in self.model.names()}
            self.id_to_name = {self.name_to_id[name]: name for name in self.name_to_id}

            # Stores all terminal/evidence nodes & non-terminal nodes
            self.terminal_nodes = []
            self.non_terminal_nodes = []
            for node_name in self.name_to_id:
                parents = [self.id_to_name[node_id] for node_id in self.model.parents(node_name)]
                if len(parents) == 0:
                    self.terminal_nodes.append(node_name)
                else:
                    self.non_terminal_nodes.append(node_name)

            # Get all nodes that will be deactivated
            self.deactivated_nodes = network_util.get_deactivated_nodes(self.model, remove_nodes.copy(),
                                                                        self.non_terminal_nodes, self.id_to_name)
            if self.print_logs:
                logging.info(f"deactivated nodes: {self.deactivated_nodes}")

            # replace CPDs of all deactivated nodes
            network_util.add_deactivate_cpd_to_model(self.deactivated_nodes, self.model)
            self.network = gum.VariableElimination(self.model)

            self.edges = [{"from": self.id_to_name[_e[0]], 'to': self.id_to_name[_e[1]]} for _e in self.model.arcs()]

            self.state_dict = {_name: self.model.variable(_name).labels() for _name in self.model.names()}
            # # Initializing dictionary to store state list for each node
            # self.state_dict = {}
            # for node in self.model.nodes():
            #     self.state_dict.update(self.network.query(variables=[node], show_progress=False).state_names)
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def get_inference(self, data):
        try:
            evidence_dict = self.create_evidence_dict(data)

            # remove evidences for all deactivated nodes
            for node in self.deactivated_nodes:
                if node in evidence_dict:
                    evidence_dict.pop(node)

            # query the network
            self.network.setEvidence(evidence_dict)
            self.network.makeInference()

            result = {}
            for node in self.non_terminal_nodes:
                result[node] = np.round(self.network.posterior(node).toarray(), 3)

            return result
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def create_evidence_dict(self, data):
        """
        creates evidence dict for the model for terminal & activated nodes
        :param data:
        :return:
        """
        """Creates the evidence dict in the format required by pgmpy network"""
        try:
            evidence_dict = {}
            # creating input node and evidence mapping
            for node in self.terminal_nodes:
                if data[node.lower()].lower() in ['yes', 'no']:
                    evidence_dict[node] = data[node.lower()].lower()
                else:
                    evidence_dict[node] = 'no'
            return evidence_dict
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e