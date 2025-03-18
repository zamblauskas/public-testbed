import redis
from redis.cluster import RedisCluster, ClusterNode
import time
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

redis_nodes = [
    ClusterNode("localhost", 6379),
    ClusterNode("localhost", 6380),
    ClusterNode("localhost", 6381)
]

try:
    redis_client = RedisCluster(
        startup_nodes=redis_nodes,
        decode_responses=True,
        require_full_coverage=False,
        retry_on_timeout=True,
        skip_full_coverage_check=True
    )
    redis_client.ping()
    logger.info("Successfully connected to Redis Cluster")
except (redis.exceptions.ConnectionError, redis.exceptions.RedisClusterException) as e:
    logger.warning(f"Failed to connect to Redis Cluster, trying standalone mode: {e}")
    try:
        redis_client = redis.Redis(host="localhost", port=6379, decode_responses=True)
        redis_client.ping()
        logger.info("Successfully connected to Redis in standalone mode")
    except redis.exceptions.ConnectionError as e:
        logger.error(f"Failed to connect to Redis: {e}")
        raise

def process_transaction(transaction_id):
    """Process a transaction with the given ID."""
    logger.info(f"Processing transaction {transaction_id}")
    time.sleep(2)
    logger.info(f"Transaction {transaction_id} processed successfully")

def handle_transaction(transaction_id):
    """Handle a transaction with distributed locking."""
    if not isinstance(transaction_id, (int, str)):
        logger.error(f"Invalid transaction ID: {transaction_id}")
        return False
    
    lock_name = f"transaction_lock_{transaction_id}"
    lock_timeout = 10  # seconds
    
    try:
        lock = redis_client.lock(name=lock_name, timeout=lock_timeout)
        
        if lock.acquire(blocking=True, blocking_timeout=5):
            try:
                logger.info(f"Lock acquired for transaction {transaction_id}")
                process_transaction(transaction_id)
                return True
            except Exception as e:
                logger.error(f"Error processing transaction {transaction_id}: {e}")
                return False
            finally:
                try:
                    lock.release()
                    logger.info(f"Lock released for transaction {transaction_id}")
                except Exception as e:
                    logger.error(f"Error releasing lock for transaction {transaction_id}: {e}")
        else:
            logger.warning(f"Could not acquire lock; transaction {transaction_id} is already being processed")
            return False
    except Exception as e:
        logger.error(f"Error while handling transaction {transaction_id}: {e}")
        return False

if __name__ == "__main__":
    handle_transaction(123)
