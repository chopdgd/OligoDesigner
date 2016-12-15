package edu.chop.dgd.dgdObjects;

/**
 * Created by jayaramanp on 11/10/16.
 */
/**
 * A directed, weighted edge in a graph
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <OligoObject>
 */
public class Edge<OligoObject> {
    private Vertex<OligoObject> from;

    private Vertex<OligoObject> to;

    private int cost;

    private boolean mark;

    /**
     * Create a zero cost edge between from and to
     *
     * @param from
     *          the starting vertex
     * @param to
     *          the ending vertex
     */
    public Edge(Vertex<OligoObject> from, Vertex<OligoObject> to) {
        this(from, to, 0);
    }

    /**
     * Create an edge between from and to with the given cost.
     *
     * @param from
     *          the starting vertex
     * @param to
     *          the ending vertex
     * @param cost
     */
    public Edge(Vertex<OligoObject> from, Vertex<OligoObject> to, int cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
        mark = false;
    }

    /**
     * Get the ending vertex
     *
     * @return ending vertex
     */
    public Vertex<OligoObject> getTo() {
        return to;
    }

    /**
     * Get the starting vertex
     *
     * @return starting vertex
     */
    public Vertex<OligoObject> getFrom() {
        return from;
    }

    /**
     * Get the cost of the edge
     *
     * @return cost of the edge
     */
    public int getCost() {
        return cost;
    }

    /**
     * Set the mark flag of the edge
     *
     */
    public void mark() {
        mark = true;
    }

    /**
     * Clear the edge mark flag
     *
     */
    public void clearMark() {
        mark = false;
    }

    /**
     * Get the edge mark flag
     *
     * @return edge mark flag
     */
    public boolean isMarked() {
        return mark;
    }

    /**
     * String rep of edge
     *
     * @return string rep with from/to vertex names and cost
     */
    public String toString() {
        StringBuffer tmp = new StringBuffer("Edge[from: ");
        tmp.append(from.getName());
        tmp.append(",to: ");
        tmp.append(to.getName());
        tmp.append(", cost: ");
        tmp.append(cost);
        tmp.append("]");
        return tmp.toString();
    }
}