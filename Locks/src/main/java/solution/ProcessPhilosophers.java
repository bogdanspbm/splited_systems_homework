package solution;

import internal.Environment;
import objects.Obj2IntBool;

import java.util.ArrayList;
import java.util.List;

public class ProcessPhilosophers implements MutexProcess {
    private final Environment env;
    boolean isLocked = false;
    boolean wantLock = false;

    private Obj2IntBool[] edges;

    private List<Integer> requestOrder = new ArrayList<>();

    public ProcessPhilosophers(Environment env) {
        this.env = env;
        initEdges();
    }

    @Override
    public void onMessage(int sourcePid, Object message) {
        int msg = (int) message;

        switch (msg) {
            case 0: {
                edges[sourcePid].invert();
                if (wantLock) {
                    tryToLock();
                }
                break;
            }
            case 1: {
                if (isLocked) {
                    requestOrder.add(sourcePid);
                } else {
                    giveBranch(sourcePid);
                }
                break;
            }
        }
    }

    @Override
    public void onLockRequest() {
        requestBranches();
        tryToLock();
    }

    @Override
    public void onUnlockRequest() {
        env.unlock();
        isLocked = false;
        invertAllEdgesAnon();
        giveOrderBranches();
    }

    private boolean canLock() {
        for (Obj2IntBool branch : edges) {
            if (branch != null && branch.b == env.getProcessId() && branch.flag == true) {
                return false;
            }
        }
        return true;
    }

    private void tryToLock() {
        if (canLock()) {
            isLocked = true;
            env.lock();
            wantLock = false;
        }
    }

    private void requestBranches() {
        wantLock = true;
        // Тут может быть проблема, что запрашиваем мнимую ветвь
        for (Obj2IntBool branch : edges) {
            if (branch != null && branch.b == env.getProcessId() && branch.flag) {
                env.send(branch.a, 1);
            }
        }
    }

    private void requestBranch(int target) {
        env.send(target, 1);
    }

    private void giveOrderBranches() {
        for (int i : requestOrder) {
            giveBranch(i);
        }

        requestOrder = new ArrayList<>();
    }

    private void giveBranch(int target) {
        Obj2IntBool branch = edges[target];

        if (!branch.flag) {
            env.send(target, 0);
            edges[target].flag = true;
            return;
        }

        if (branch.flag) {
            env.send(target, 0);
            edges[target].invert();

            if (wantLock) {
                requestBranch(target);
            }
        }
    }


    private void invertAllEdgesAnon() {
        for (Obj2IntBool edge : edges) {
            if (edge != null) {
                edge.invertAnon();
            }
        }
    }

    private void initEdges() {
        edges = new Obj2IntBool[env.getNumberOfProcesses() + 1];
        edges[env.getProcessId()] = null;
        for (int i = 1; i <= env.getNumberOfProcesses(); i++) {
            if (i > env.getProcessId()) {
                edges[i] = new Obj2IntBool(env.getProcessId(), i, true);
            } else if (i < env.getProcessId()) {
                edges[i] = new Obj2IntBool(i, env.getProcessId(), true);
            }
        }
    }

}