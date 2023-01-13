package solution;

import internal.Environment;

import java.util.Map;

public class DijkstraProcessImpl implements DijkstraProcess {

    private final Environment env;

    private int msgCounter = 0;
    private int chdCounter = 0;
    private int parentID = -1;
    private long distToStart = -1;
    private boolean isRed = false;
    private boolean isRoot = false;

    public DijkstraProcessImpl(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int senderPid, Object rawMessage) {
        String msg = (String) rawMessage;
        switch (msg) {
            case "a": {
                this.msgCounter--;
                break;
            }
            case "c": {
                this.chdCounter++;
                break;
            }
            case "r": {
                this.chdCounter--;

                if (this.isRoot && this.checkCanAFK()) {
                    env.finishExecution();
                    return;
                }
                break;
            }
            default: {
                updateDistance(senderPid, Long.parseLong(msg));
            }
        }

        this.tryToGoAFK();
    }

    @Override
    public Long getDistance() {
        if (this.distToStart == -1) {
            return null;
        } else {
            return this.distToStart;
        }
    }

    @Override
    public void onComputationStart() {
        this.distToStart = 0;
        this.isRed = true;
        this.isRoot = true;

        for (Map.Entry<Integer, Long> entry : env.getNeighbours().entrySet()) {
            if (env.getProcessId() == entry.getKey()) {
                continue;
            }

            env.send(entry.getKey(), "" + entry.getValue());
            this.msgCounter++;
        }

        if (this.msgCounter == 0) {
            env.finishExecution();
        }
    }

    private void updateDistance(int toID, long distance) {
        if (!this.isRed) {
            assert this.parentID == -1;
            this.isRed = true;
            this.parentID = toID;
            env.send(toID, "c");
        }

        if (this.distToStart > distance) {
            this.distToStart = distance;

            for (Map.Entry<Integer, Long> entry : env.getNeighbours().entrySet()) {
                if (env.getProcessId() == entry.getKey()) {
                    continue;
                }

                env.send(entry.getKey(), "" + (this.distToStart + entry.getValue()));
                this.msgCounter++;
            }
        }
        env.send(toID, "a");
    }

    private void tryToGoAFK() {
        if (this.isRed && this.checkCanAFK()) {
            this.isRed = false;
            assert this.parentID != env.getProcessId() && this.parentID != -1;
            env.send(this.parentID, "r");
            this.parentID = -1;
        }
    }

    private boolean checkCanAFK() {
        return this.chdCounter == 0 && this.msgCounter == 0;
    }
}
