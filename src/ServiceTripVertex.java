import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class ServiceTripVertex extends  ServiceTripData implements STGroupVertex {
    private final TreeMap<Integer, Edge> edgesSubsequentTrips;
    private final Edge[] edgesSubsequentCes;
    private final Edge[] edgesPreviousCes;

    public ServiceTripVertex(ServiceTripData serviceTripData, TreeMap<Integer, Edge> edgesSubsequentTrips,
                             LinkedList<Edge> edgesSubsequentCesList, LinkedList<Edge> edgesPreviousCesList) {
        super(serviceTripData.getIndex(), serviceTripData.getId(), serviceTripData.getStart(),
                serviceTripData.getEnd(), serviceTripData.getDistance(), serviceTripData.getConsumption());
        this.edgesSubsequentTrips = new TreeMap<>(edgesSubsequentTrips);

        this.edgesSubsequentCes = (Edge[]) edgesSubsequentCesList.toArray();
        this.edgesPreviousCes = (Edge[]) edgesPreviousCesList.toArray();
    }

    public Edge getEdgeForSubsequentTrip(int subsequentTripId) {
        return edgesSubsequentTrips.get(subsequentTripId);
    }

    public Edge getEdgeForSubsequentCE(int subsequentChargerId) {
        return edgesSubsequentCes[subsequentChargerId];
    }

    public Edge getEdgeForPreviousCE(int previousChargerId) {
        return edgesPreviousCes[previousChargerId];
    }
}
