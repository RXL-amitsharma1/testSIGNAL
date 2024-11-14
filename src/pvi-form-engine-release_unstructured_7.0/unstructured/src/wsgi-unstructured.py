from unstructure_pipeline import app
from configs import global_config , load_updated_config
from semantic_embedding import update_semantic_embeddings
from redis_client import RedisClient

redis_obj = RedisClient()
load_updated_config(redis_obj, raise_on_failure=True)
update_semantic_embeddings(redis_obj)
print("Codelist saved to Redis, semantic embedding updated from wsgi")

if __name__ == "__main__":
    app.run(host=global_config["PIPELINE_API"]["URL"], port=global_config["PIPELINE_API"]["PORT"])
