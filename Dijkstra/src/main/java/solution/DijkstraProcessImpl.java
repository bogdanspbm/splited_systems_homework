package solution;

import internal.Environment;

public class DijkstraProcessImpl implements DijkstraProcess {
    private final Environment env;

    private int parentID = -1;
    private int msgCounter = 0;
    private int chdCounter = 0;

    private long distToStart = -1;

    public DijkstraProcessImpl(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int senderPid, Object message) {
        String msg = (String) message;

        switch (msg) {
            case "a": {
                msgCounter -= 1;
                checkCanAFK();
                break;
            }
            case "c": {
                chdCounter += 1;
                msgCounter -= 1;
                break;
            }
            case "r": {
                chdCounter -= 1;
                checkCanAFK();
                break;
            }
            default: {
                Long value = Long.parseLong(msg);
                updateDistance(senderPid, value);

                if (parentID == -1) {
                    requestChild(senderPid);
                    doCalculation();
                }
            }
        }
    }

    private void checkCanAFK() {
        if (chdCounter == 0 && msgCounter == 0) {
            env.send(parentID, "r");
        }
    }

    private void doCalculation() {
        for (int key : env.getNeighbours().keySet()) {
            env.send(key, distToStart + "");
            msgCounter += 1;
        }
    }


    private void requestChild(int toID) {
        env.send(toID, "c");
        parentID = toID;
    }

    private void updateDistance(int target, long value) {
        if (distToStart == -1) {
            distToStart = value + env.getNeighbours().get(target);
        } else {
            distToStart = Math.max(value + env.getNeighbours().get(target), distToStart);
        }
    }

    @Override
    public Long getDistance() {
        return distToStart;
    }

    @Override
    public void onComputationStart() {
        parentID = env.getProcessId();
        distToStart = 0;
        doCalculation();
    }
}
