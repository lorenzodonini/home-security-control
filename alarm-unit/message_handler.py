import paho.mqtt.client as mqtt


class MessageHandler:
    def __init__(self, qos=0):
        self.topics = dict()  # <topic, callback>
        self.qos = qos
        self.client = mqtt.Client()
        self.client.on_connect = self.__on_connect__
        self.client.on_message = self.__on_message__

    def __on_connect__(self, client, userdata, flags, rc):
        print("Connected with result code "+str(rc))
        for topic in self.topics:
            self.client.subscribe(topic, self.qos)

    def __on_message__(self, client, userdata, msg):
        print(client + " -> " + msg)

    def subscribe(self, topic, callback):
        self.topics[topic] = callback
        self.client.subscribe(topic, self.qos)
        self.client.message_callback_add(topic, callback)

    def unsubscribe(self, topic):
        self.client.message_callback_remove(topic)
        del self.topics[topic]

    def start_dispatcher(self, ip, port=1883, keepalive=60):
        self.client.connect(ip, port, keepalive)

    def loop(self):
        self.client.loop_forever()