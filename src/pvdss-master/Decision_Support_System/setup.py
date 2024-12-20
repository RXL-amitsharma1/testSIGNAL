import os
import sysconfig
from setuptools import setup, find_packages
from setuptools.command.build_py import build_py as _build_py
from Cython.Build import cythonize


EXCLUDE_FILES = [
    './setup.py', './manage.py',
    './DSS/__init__.py', './DSS/tests/__init__.py',
    './DSS/migrations/__init__.py', './Decision_Support_System/__init__.py',

]


# noinspection PyPep8Naming
class build_py(_build_py):

    def find_package_modules(self, package, package_dir):
        ext_suffix = sysconfig.get_config_var('EXT_SUFFIX')
        modules = super().find_package_modules(package, package_dir)
        filtered_modules = []
        for (pkg, mod, filepath) in modules:
            if os.path.exists(filepath.replace('.py', ext_suffix)):
                continue
            filtered_modules.append((pkg, mod, filepath, ))
        return filtered_modules


def get_ext_paths(root_dir, exclude_files):
    """get filepaths for compilation"""
    paths = []

    for root, dirs, files in os.walk(root_dir):
        for filename in files:
            if os.path.splitext(filename)[1] != '.py':
                continue

            file_path = os.path.join(root, filename)
            if file_path in exclude_files:
                continue

            paths.append(file_path)
    return paths


setup(
    name='python',
    version='1',
    packages=find_packages(),
    include_package_data=True,
    ext_modules=cythonize(
        get_ext_paths('.', EXCLUDE_FILES),
        compiler_directives={'language_level': 3}
    ),
    cmdclass={
        'build_py': build_py
    }
)



