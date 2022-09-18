package solution;

import internal.Environment;
import objects.Obj2Int;

import java.util.ArrayList;
import java.util.List;

public class ProcessRickartAgrawala implements MutexProcess {
    private final Environment env;

    private int lamportTime = 1;

    private int myRequestTime = -1;

    private int acceptCounter = 0;

    private List<Obj2Int> otherRequestTimes = new ArrayList<>();
    private List<Integer> okOrder = new ArrayList<>();

    public ProcessRickartAgrawala(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int sourcePid, Object message) {
        lamportTime += 1;

        int[] msg = (int[]) message;

        updateLamportTime(msg[0]);

        switch (msg[1]) {
            case 1: {
                otherRequestTimes.add(new Obj2Int(sourcePid, msg[0]));

                if (myRequestTime == -1 || myRequestTime < msg[0] || (myRequestTime == msg[0] && env.getProcessId() < sourcePid)) {
                    sendOk(sourcePid, msg[0]);
                } else {
                    okOrder.add(sourcePid);
                }
                break;
            }

            case 0: {
                acceptCounter += 1;
                tryToDoLock();
                break;
            }
        }
    }

    @Override
    public void onLockRequest() {
        lamportTime += 1;
        myRequestTime = lamportTime;

        sendRequest();

        tryToDoLock();
    }

    @Override
    public void onUnlockRequest() {
        lamportTime += 1;

        env.unlock();
        sendRelease();
        myRequestTime = -1;
    }

    private void sendRelease() {
        for (Integer id : okOrder) {
            sendOk(id, lamportTime);
        }

        okOrder = new ArrayList<>();
    }

    private void sendOk(int target, int time) {
        //MessageSerializable msg = new MessageSerializable(new ObjIntString(time, "o"));
        int[] msg = {time, 0};
        env.send(target, msg);
    }

    private void updateLamportTime(int msgTime) {
        lamportTime = Math.max(lamportTime, msgTime + 1);
    }


    private void tryToDoLock() {
        if (acceptCounter == env.getNumberOfProcesses() - 1) {
            if (checkIsFirstInOrder()) {
                env.lock();
            }
        }
    }

    private boolean checkIsFirstInOrder() {
        for (Obj2Int val : otherRequestTimes) {
            if (val.b() < myRequestTime) {
                return false;
            }
            if (val.b() == myRequestTime) {
                if (val.a() < env.getProcessId()) {
                    return false;
                }
            }
        }
        return true;
    }


    private void sendRequest() {
        acceptCounter = 0;

        int[] msg = {myRequestTime, 1};
        for (int i = 1; i <= env.getNumberOfProcesses(); i++) {
            if (i != env.getProcessId()) {
                env.send(i, msg);
            }
        }
    }
}
