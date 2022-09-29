package solution;

import internal.Environment;
import objects.MessageSerializable;

import java.util.LinkedList;
import java.util.Queue;

public class ProcessCentralized implements MutexProcess {
    private final Environment env;

    Queue<Integer> okQueue = new LinkedList<>();

    boolean isLocked = false;


    public ProcessCentralized(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int sourcePid, Object message) {

        String msg = (String) message;

        switch (msg) {
            case "o": {
                lock();
                break;
            }
            case "r": {
                okQueue.add(sourcePid);
                sendOk(sourcePid);
                break;
            }
            case "l": {
                sendRelease();
                break;
            }
        }
    }

    private void sendOk(int toID) {
        if (okQueue.size() > 0 && toID == okQueue.peek() && isLocked == false) {
            isLocked = true;
            if (toID != 1) {
                env.send(toID, "o");
            } else {
                lock();
            }
        }
    }

    private void requestOk() {
        if (env.getProcessId() == 1) {
            okQueue.add(1);
            sendOk(1);
        } else {
            env.send(1, "r");
        }
    }

    private void sendRelease() {
        if (env.getProcessId() == 1) {
            isLocked = false;
            okQueue.poll();
            if (!okQueue.isEmpty()) {
                sendOk(okQueue.peek());
            }
        } else {
            env.send(1, "l");
        }
    }

    private void lock() {
        env.lock();
    }


    @Override
    public void onLockRequest() {
        requestOk();
    }

    @Override
    public void onUnlockRequest() {
        env.unlock();
        sendRelease();
    }
}
