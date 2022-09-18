package solution;

import internal.Environment;
import objects.MessageSerializable;


public class ProcessToken implements MutexProcess {
    private final Environment env;

    private boolean hasToken = false;
    private boolean waitForLock = false;

    public ProcessToken(Environment env) {
        this.env = env;
        if(env.getProcessId() == 1){
            hasToken = true;

            if(!waitForLock){
                shareTokenNext();
            }
        }
    }

    @Override
    public void onMessage(int sourcePid, Object message) {
        hasToken = true;
        if(waitForLock){
            tryDoLock();
        } else {
          shareTokenNext();
        }
    }

    @Override
    public void onLockRequest() {
        waitForLock = true;
        tryDoLock();
    }

    @Override
    public void onUnlockRequest() {
        env.unlock();

        shareTokenNext();
    }

    private void shareTokenNext(){
        if(hasToken){
            hasToken = false;

            int nextID = (env.getProcessId() + 1) % env.getNumberOfProcesses();
            if(nextID == 0){
                nextID = env.getNumberOfProcesses();
            }

            env.send(nextID, new MessageSerializable("t"));
        }
    }

    private void tryDoLock(){
        if(hasToken && waitForLock){
            env.lock();
            waitForLock = false;
        }
    }

}
