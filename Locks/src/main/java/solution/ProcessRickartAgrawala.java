package solution;

import internal.Environment;
import objects.Obj2Int;

import java.util.ArrayList;
import java.util.List;

public class ProcessRickartAgrawala implements MutexProcess {
    private final Environment env;

    private int requestTime = -1;

    private int curTime = 0;

    private int okCounter = 0;

    private boolean inLock = false;

    private List<Integer> orderOk = new ArrayList<>();


    public ProcessRickartAgrawala(Environment env) {
        this.env = env;
    }

    @Override
    public void onMessage(int sourcePid, Object message) {
        try {
            // Req
            int[] msg = (int[]) (message);
            curTime = Math.max(msg[1] + 1, curTime + 1);

            // Если мы не хотим зайти или наше время больше и не в блокировке, отправляем окей
            if ((msg[0] < requestTime || requestTime == -1 || (msg[0] == requestTime && env.getProcessId() > sourcePid)) && !inLock) {
                sendOkey(sourcePid);
            } else if (!orderOk.contains(sourcePid)) {
                orderOk.add(sourcePid);
            }

        } catch (Exception e) {
            // Okey
            if (requestTime != -1) {
                //System.out.println("Ok Receive: " + env.getProcessId());
                okCounter += 1;
                tryEnterLock();
            }
        }
    }

    @Override
    public void onLockRequest() {
        if (requestTime == -1) {
            increaseCurTime();
            requestTime = curTime;

           // System.out.println("Lock Request: " + env.getProcessId() + " Time: " + curTime);

            sendRequest();
            tryEnterLock();
        }
    }

    @Override
    public void onUnlockRequest() {
        increaseCurTime();

        // Разблокируем
        env.unlock();
        inLock = false;
        requestTime = -1;

        //System.out.println("Unlock: " + env.getProcessId() + " Order Size: " + orderOk.size());


        // Отправляем окей из очереди
        for (int i : orderOk) {
            sendOkey(i);
        }

        orderOk = new ArrayList<>();
    }

    private void increaseCurTime() {
        curTime += 1;
    }

    private void sendRequest() {
        // Обнуляем счетчик океев
        okCounter = 1;

        // Отправляем всем запрос с нашим временем запроса
        for (int i = 1; i <= env.getNumberOfProcesses(); i++) {
            if (i != env.getProcessId()) {
                increaseCurTime();
                int[] msg = {requestTime, curTime};
                env.send(i, msg);
            }
        }
    }

    private void sendOkey(int target) {
        //System.out.println("Send Ok: " + env.getProcessId());
        increaseCurTime();
        env.send(target, curTime);
    }

    private void tryEnterLock() {
        if (okCounter == env.getNumberOfProcesses()) {
            //System.out.println("Lock: " + env.getProcessId());
            env.lock();
            inLock = true;
        }
    }

}
