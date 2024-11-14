FROM python:3.12.3-slim


RUN apt-get update && apt-get install -y --no-install-recommends \
    mecab mecab-ipadic-utf8 libmecab-dev \
    gcc \
    g++ \
    build-essential && \
    apt install -y redis-server && \
    apt-get upgrade -y openssl systemd && \
    rm -rf /var/lib/apt/lists/*

COPY ./ /unstructured/

RUN pip install --no-deps --no-cache-dir -r /unstructured/requirements.txt && \
    rm /unstructured/build.sh && \
    rm /unstructured/run.sh && \
    python -m nltk.downloader stopwords && \
    python -m nltk.downloader punkt && \
    python -m spacy download en_core_web_sm

#unstructured
EXPOSE 9888
#biobert
EXPOSE 10001
#spacy
EXPOSE 10002
#openai
EXPOSE 10003
#coref
EXPOSE 10004

WORKDIR /unstructured

#EXPOSE 8000
#CMD [ "python", "-m", "http.server" ]
RUN chmod +x run_in_docker.sh
CMD ["./run_in_docker.sh"]
