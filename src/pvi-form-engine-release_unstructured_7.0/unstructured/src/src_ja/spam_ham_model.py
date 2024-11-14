import re
import os
import sys
import unicodedata
from konoha import WordTokenizer
from spacy.lang.ja.stop_words import STOP_WORDS
import spacy
import logs
logger = logs.get_logger()
default_error_message = "Inside spam_ham_model.py - An error occured on line {}. Exception type: {} , Exception: {} "


tokenizer = WordTokenizer('MeCab')


class SpamIdentification:
    def __init__(self, model_folder_path):
        # email_model_name = "spam_ham_ja"
        self.model_email = self.load(model_folder_path, "spam_ham_ja")

    def predict(self, text):
        try:
            preprocessed_text = self.preprocess(text)
            logger.debug(f"Pre Processed Text: \n {preprocessed_text}")
            doc = self.model_email(preprocessed_text)
            return doc.cats
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def preprocess(self, text):
        try:
            normalizer = Normalizer()
            # Normalize neologd
            text = normalizer.unicode_normalize('0-9A-Za-z', text)

            # Remove hyphens, choonpus, tildes
            text = re.sub('[˗֊‐‑‒–⁃⁻₋−]+', '-', text)  # normalize hyphens
            text = re.sub('[﹣－ｰ—―─━ー]+', 'ー', text)  # normalize choonpus
            text = re.sub('[~∼∾〜〰～]', '', text)  # remove tildes
            # text = re.sub('[0-9]', '', text)

            # Translate specific characters
            translation_table = normalizer.maketrans(
                '!"#$%&\'()*+,-./:;<=>?@[¥]^_`{|}~｡､･｢｣',
                '！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝〜。、・「」'
            )
            text = text.translate(translation_table)

            # Remove other special characters
            text = re.sub(re.compile("[!-/:-@[-`{-~]"), '', text)
            text = re.sub(re.compile('[!"#$%&\'()*+,-./:;<=>?@[¥]^_`{|}~｡､･｢｣]'), '', text)
            text = re.sub(re.compile('[■□◆◇◯“…【】『』！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝〜。、・「」]'), '', text)

            # Remove extra spaces
            text = normalizer.remove_extra_spaces(text)

            # Normalize specific characters
            text = normalizer.unicode_normalize('！”＃＄％＆’（）＊＋，－．／：；＜＞？＠［￥］＾＿｀｛｜｝〜', text)

            # Additional preprocessing
            text = text.replace('\r', '').replace('\n', '').replace('件名：', '').replace('再：', '').replace('’',
                                                                                                            '\'').replace(
                '”', '"')

            # Tokenization
            tokens = tokenizer.tokenize(text)
            jpn_symbols = ['’', '”', '‘', '。', '、', 'ー', '！', '？', '：', '；', '（', '）', '＊', '￥']
            eng_symbols = ["'", '"', '`', '.', ',', '-', '!', '?', ':', ';', '(', ')', '*', '--', '\\']
            stopwords = list(STOP_WORDS) + jpn_symbols + eng_symbols

            clean_tokens = [x.surface for x in tokens if x.surface not in stopwords]

            text = "".join(clean_tokens)

            return text
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def load(self, folder, model):
        try:
            return spacy.load(os.path.join(folder, model))
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


class Normalizer:
    def unicode_normalize(self, cls, s):
        try:
            pt = re.compile('([{}]+)'.format(cls))
            def norm(c):
                return unicodedata.normalize('NFKC', c) if pt.match(c) else c
            s = ''.join(norm(x) for x in re.split(pt, s))
            return s
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def remove_extra_spaces(self, s):
        try:
            s = re.sub('[  ]+', ' ', s)
            blocks = ''.join((
                        '\u4E00-\u9FFF',  # CJK UNIFIED IDEOGRAPHS
                        '\u3040-\u309F',  # HIRAGANA
                        '\u30A0-\u30FF',  # KATAKANA
                        '\u3000-\u303F',  # CJK SYMBOLS AND PUNCTUATION
                        '\uFF00-\uFFEF'   # HALFWIDTH AND FULLWIDTH FORMS
                        ))
            basic_latin = '\u0000-\u007F'
            s = self.remove_space_between(blocks, blocks, s)
            s = self.remove_space_between(blocks, basic_latin, s)
            s = self.remove_space_between(basic_latin, blocks, s)
            return s
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def remove_space_between(self, cls1, cls2, s):
        try:
            p = re.compile('([{}]) ([{}])'.format(cls1, cls2))
            while p.search(s):
                s = p.sub(r'\1\2', s)
            return s
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

    def maketrans(self, f, t):
        try:
            return {ord(x): ord(y) for x, y in zip(f, t)}
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e