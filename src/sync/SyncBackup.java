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
        

        secActivity = true;

        Message response = new MessageImpl();
        response.setType("backup_ack");

        return response;
    }

}
