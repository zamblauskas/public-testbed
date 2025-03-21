import json
import random
from decimal import Decimal
from faker import Faker
from kafka import KafkaProducer
from typing import Dict, Any

fake = Faker()

customer_ids = [fake.uuid4() for _ in range(6)]

def create_random_order(sequence: int) -> Dict[str, Any]:
    customer_id = random.choice(customer_ids)
    return {
        "id": fake.uuid4(),
        "amount": str(Decimal(random.uniform(-1000.0, 1000.0)).quantize(Decimal("0.01"))),
        "customerId": customer_id,
        "sequence": sequence
    }

def main():
    servers = ["localhost:29092", "localhost:29093", "localhost:29094"]
    topic = "test-2"

    producer = KafkaProducer(
        bootstrap_servers=servers,
        value_serializer=lambda v: json.dumps(v).encode('utf-8'),
        acks='all'
    )

    try:
        for i in range(100):
            order = create_random_order(i)
            producer.send(topic, key=order["id"].encode('utf-8'), value=order)
            print(f"Published order: {order}")

        producer.flush()
    except Exception as e:
        print(f"Error publishing messages: {e}")
    finally:
        producer.close()

if __name__ == "__main__":
    main()
