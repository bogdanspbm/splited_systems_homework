package solution;

import internal.Environment;
import objects.MessageSerializable;

import java.util.LinkedList;
import java.util.Queue;

public class ProcessCentralized implements MutexProcess {
    private final Environment env;

    Queue<Integer> lockQueue = new LinkedList<>();

    public ProcessCentralized(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int sourcePid, Object message) {
        MessageSerializable msg = (MessageSerializable) message;
        String content = (String) msg.content();

        switch (content) {
            // req
            case "q": {
                lockQueue.add(sourcePid);
                if (sourcePid == lockQueue.peek()) {
                    sendOkMessage(sourcePid);
                }
                break;
            }
            // rel
            case "l": {
                if (sourcePid == lockQueue.peek()) {
                    lockQueue.poll();

                    if (!lockQueue.isEmpty()) {
                        int toID = lockQueue.peek();
                        sendOkMessage(toID);
                    }
                }
                break;
            }
            // ok
            case "o": {
                env.lock();
                break;
            }
        }
    }

    private void sendOkMessage(int toID) {
        if (toID != 1) {
            String body = "o";
            MessageSerializable ans = new MessageSerializable(body);
            env.send(toID, ans);
        } else {
            env.lock();
        }
    }

    @Override
    public void onLockRequest() {
        int procID = env.getProcessId();
        if (procID != 1) {
            String body = "q";
            MessageSerializable msg = new MessageSerializable(body);
            env.send(1, msg);
        } else {
            lockQueue.add(1);
            if (lockQueue.peek() == 1) {
                env.lock();
            }
        }
    }

    @Override
    public void onUnlockRequest() {
        int procID = env.getProcessId();
        if (procID != 1) {
            String body = "l";
            MessageSerializable msg = new MessageSerializable(body);
            env.send(1, msg);
            env.unlock();
        } else {
            lockQueue.poll();
            env.unlock();

            if (!lockQueue.isEmpty()) {
                String body = "o";
                int toID = lockQueue.peek();
                MessageSerializable ans = new MessageSerializable(body);
                env.send(toID, ans);
            }
        }
    }
}
