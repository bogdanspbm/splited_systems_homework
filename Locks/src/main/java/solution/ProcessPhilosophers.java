package solution;

import internal.Environment;
import objects.Obj2IntBool;

import java.util.ArrayList;
import java.util.List;

public class ProcessPhilosophers implements MutexProcess {
    private final Environment env;

    private List<Obj2IntBool> listBranches = new ArrayList<>();
    private List<Integer> requestOrder = new ArrayList<>();

    boolean isLocked = false;
    boolean wantToLock = false;

    public ProcessPhilosophers(Environment env) {
        this.env = env;
        initBranches();
    }

    @Override
    public void onMessage(int sourcePid, Object message) {

        int msg = (int) message;

        switch (msg) {
            case 0: {
                invertBranch(sourcePid);
                tryToLock();
                break;
            }
            case 1: {
                if (isLocked) {
                    requestOrder.add(sourcePid);
                } else {
                    giveBranch(sourcePid);
                    if (wantToLock) {
                        requestBranch(sourcePid);
                    }
                }
                break;
            }

            case -1: {
                for (Obj2IntBool branch : listBranches) {
                    System.out.println(branch);
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
        invertBranchesAnon();
        giveBranchesForOrder();
    }

    private boolean canLock() {
        for (Obj2IntBool branch : listBranches) {
            if ((branch.b == env.getProcessId() && branch.flag == true)) {
                return false;
            }
        }
        return true;
    }


    private void tryToLock() {
        if (canLock()) {
            isLocked = true;
            env.lock();
            wantToLock = false;
        }
    }

    private void invertBranchesAnon() {
        for (Obj2IntBool branch : listBranches) {
            branch.invertAnon();
        }
    }

    private void invertBranch(int targetID) {
        for (Obj2IntBool branch : listBranches) {
            if (branch.a == targetID) {
                branch.invert();
                return;
            }
        }
    }

    private void requestBranches() {
        wantToLock = true;
        // Тут может быть проблема, что запрашиваем мнимую ветвь
        for (Obj2IntBool branch : listBranches) {
            if (branch.b == env.getProcessId() && branch.flag) {
                env.send(branch.a, 1);
            }
        }
    }

    private void requestBranch(int targetID) {
        env.send(targetID, 1);
    }

    private void giveBranchesForOrder() {
        for (int i : requestOrder) {
            giveBranch(i);
        }
    }

    private void giveBranch(int targetID) {
        for (Obj2IntBool branch : listBranches) {
            if ((branch.a == targetID && branch.flag == false)) {
                env.send(targetID, 0);
                branch.flag = true;
            } else if ((branch.b == targetID && branch.flag == true)) {
                env.send(targetID, 0);
                branch.invert();
            }
        }
    }

    private void initBranches() {
        requestOrder = new ArrayList<>();
        listBranches = new ArrayList<>();

        for (int i = 1; i < env.getProcessId(); i++) {
            listBranches.add(new Obj2IntBool(i, env.getProcessId(), true));
        }
        for (int i = env.getProcessId() + 1; i <= env.getNumberOfProcesses(); i++) {
            listBranches.add(new Obj2IntBool(env.getProcessId(), i, true));
        }
    }
}