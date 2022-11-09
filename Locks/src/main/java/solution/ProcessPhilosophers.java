package solution;

import internal.Environment;
import objects.Obj2IntBool;

import java.util.ArrayList;
import java.util.List;

public class ProcessPhilosophers implements MutexProcess {
    private final Environment env;

    private int id = -1;
    boolean isLocked = false;
    boolean wantLock = false;

    private Obj2IntBool[] edges;

    private List<Integer> requestOrder = new ArrayList<>();

    public ProcessPhilosophers(Environment env) {
        this.env = env;
        id = env.getProcessId();
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

               // System.out.println("Receive Ok: " + env.getProcessId() + " From: " + sourcePid);
                break;
            }
            case 1: {
               // System.out.println("Receive Ask: " + env.getProcessId() + " From: " + sourcePid);
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
      //  System.out.println("Request: " + env.getProcessId());
        displayBranches();
        tryToLock();
        requestBranches();
    }

    @Override
    public void onUnlockRequest() {
       // System.out.println("Unlock: " + env.getProcessId());
        env.unlock();
        isLocked = false;
        wantLock = false;
        invertAllEdgesAnon();
        giveOrderBranches();

        displayBranches();
    }

    private boolean canLock() {
        displayBranches();
        for (Obj2IntBool branch : edges) {

            if (branch != null && branch.b == env.getProcessId() && branch.flag == true) {
                return false;
            }

            if (branch != null && branch.a == env.getProcessId() && branch.flag == false) {
                return false;
            }
        }
        //System.out.println("Can Lock: " + env.getProcessId());
        return true;
    }

    private void tryToLock() {
        if (canLock()) {
          //  System.out.println("Lock: " + env.getProcessId());
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
              //  System.out.println("Request branch To: " + env.getProcessId() + " From: " + branch.a);
                env.send(branch.a, 1);
            }
        }
        displayBranches();
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

        //System.out.println("Give Branch From: " + env.getProcessId() + " To: " + target);

        if (!branch.flag) {
            env.send(target, 0);
            edges[target].flag = true;

            if (wantLock) {
                requestBranch(target);
            }

            return;
        }

        if (branch.flag) {
            if (!wantLock) {
                env.send(target, 0);
                edges[target].invert();
            } else {
                requestBranch(target);
                // requestOrder.add(target);
            }
        }

    }


    private void invertAllEdgesAnon() {
        for (Obj2IntBool edge : edges) {
            if (edge != null && edge.flag) {
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

    private void displayBranches() {
        //System.out.println("Display Branches: " + env.getProcessId());
        for (Obj2IntBool branch : edges) {
            if (branch != null) {
                //System.out.println(branch);
            }
        }
    }

}