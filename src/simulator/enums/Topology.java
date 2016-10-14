package simulator.enums;

/**
 * Created by Chris on 6/8/2016.
 */
public enum Topology {
    HyperCube, FullyConnected;

    public static Topology fromString(String s) {
        switch (s){
            default:
            case "HyperCube": return HyperCube;
            case "FullyConnected": return FullyConnected;
        }
    }
}
