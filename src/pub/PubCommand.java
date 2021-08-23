package pub;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CopyOnWriteArrayList;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;
import core.client.Client;

public class PubCommand implements PubSubCommand {

	@Override
	public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
			String sencondaryServerAddress, int secondaryServerPort) {

		Message response = new MessageImpl();
		int logId = m.getLogId();
		logId++;

		response.setLogId(logId);
		m.setLogId(logId);

		try {
			// sincronizar com o broker backup
			Message syncPubMsg = new MessageImpl();
			syncPubMsg.setBrokerId(m.getBrokerId());
			syncPubMsg.setContent(m.getContent());
			syncPubMsg.setLogId(m.getLogId());
			syncPubMsg.setType("syncPub");

			Client clientBackup = new Client(sencondaryServerAddress, secondaryServerPort);
			syncPubMsg = clientBackup.sendReceive(syncPubMsg);
			System.out.println(syncPubMsg.getContent());

		} catch (Exception e) {
			System.out.println("Cannot sync with backup - publish service");
		}

		log.add(m);

		Message msg = new MessageImpl();
		msg.setContent(m.getContent()); // conteudo == posso verificar o tipo de conteudo
		msg.setLogId(logId);
		msg.setType("notify");

		CopyOnWriteArrayList<String> subscribersCopy = new CopyOnWriteArrayList<String>(); // broker vira cliente e
																							// manda notificação pra
																							// geral
		subscribersCopy.addAll(subscribers);
		for (String aux : subscribersCopy) {
			String[] ipAndPort = aux.split(":");
			Client client = new Client(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
			msg.setBrokerId(m.getBrokerId()); // manda e espera
			Message cMsg = client.sendReceive(msg); // se não recebeu vai ser null e ele tira da lista de sub
			if (cMsg == null) {
				System.out.println("Publish service is not proceed... " + msg.getContent());
				subscribers.remove(aux);
			}
		}

		// o meu consumidor vai trabalhar sendo notificado (eu sou cliente)
		// a minha thread de consumidor vai receber a notificação

		response.setContent("Message published: " + m.getContent());
		response.setType("pub_ack");

		// depois de publicar pra geral eu sou notificado

		// se for no accquire manda mensagem de envio avisando q pode usar

		return response;

	}

}
