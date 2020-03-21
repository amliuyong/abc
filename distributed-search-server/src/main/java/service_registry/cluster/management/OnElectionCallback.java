package service_registry.cluster.management;

public interface OnElectionCallback {

    void onElectedToBeLeader();

    void onWorker();
}
