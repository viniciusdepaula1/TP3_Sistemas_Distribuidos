package sync;

import java.util.Set;
import java.util.SortedSet;
import java.util.Iterator;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;
import core.client.Client;

public class SyncBackup implements PubSubCommand {

    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
                           String sencondaryServerAddress, int secondaryServerPort, boolean secActivity) {
        if (!log.isEmpty()) {
            Iterator<Message> it = log.iterator();
            while (it.hasNext()) {                
                try {
                    Message msg = it.next();
                    Message syncPubMsg = new MessageImpl();
                    syncPubMsg.setBrokerId(msg.getBrokerId());
                    syncPubMsg.setContent(msg.getContent());
                    syncPubMsg.setLogId(msg.getLogId());
                    syncPubMsg.setType("syncPub");

                    Client clientBackup = new Client(sencondaryServerAddress, secondaryServerPort);
                    syncPubMsg = clientBackup.sendReceive(syncPubMsg);

                } catch (Exception e) {}
            }
        }

        System.out.println("SUBSCRIBERSS");
        if (!subscribers.isEmpty()) {
            Iterator<String> it = subscribers.iterator();
            while (it.hasNext()) {                
                try {
                    String msg = it.next();
                    Message syncPubMsg = new MessageImpl();
                    syncPubMsg.setBrokerId(secondaryServerPort);
                    syncPubMsg.setContent(msg);
                    syncPubMsg.setType("syncSub");
                    System.out.println(msg);

                    Client clientBackup = new Client(sencondaryServerAddress, secondaryServerPort);
                    syncPubMsg = clientBackup.sendReceive(syncPubMsg);

                } catch (Exception e) {}
            }
        }


        secActivity = true;

        Message response = new MessageImpl();
        response.setLogId(m.getLogId());
        response.setContent("Message published on backup: " + m.getContent());
        response.setType("backup_ack");

        return response;
    }

}
