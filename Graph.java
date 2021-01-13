/*
    Gregory Freitas
    Assignment 3
*/

import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Graph
{
    private ArrayList<Vertex> vertices;
    private Vertex source;
    private Integer numberOfVertices;
    private Integer numberOfEdges;

    public Graph()
    {
        vertices = new ArrayList<Vertex>();
    }

    public static void main(final String[] args) 
    {
        PrintWriter bellmanFordPw, floydWarshallPw;
        Scanner scanner;
        Graph myGraph = new Graph();

        try 
        {
            scanner = new Scanner(new File("cop3503-asn3-input.txt"));
            bellmanFordPw = new PrintWriter("cop3503-asn3-output-freitas-greg.bf.txt"); 
            floydWarshallPw = new PrintWriter("cop3503-asn3-output-freitas-greg.fw.txt");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return;
        }

        // Create all the vertices and store in aN ArrayList.
        Integer numberOfVertices = scanner.nextInt();
        myGraph.SetNumberOfVertices(numberOfVertices);
        scanner.nextLine();

        Integer sourceID = scanner.nextInt();
        scanner.nextLine();

        Integer numberOfEdges = scanner.nextInt();
        myGraph.SetNumberOfEdges(numberOfEdges);

        myGraph.CreateVertices(numberOfVertices, sourceID);

        // While there is more tokens to read, loop.
        while (scanner.hasNext()) 
        {
            // If the next token is not an Int, then skip this line.
            if (!scanner.hasNextInt())
                scanner.nextLine();
            else 
            {
                // Read the first two numbers and get a reference to their Vertex.
                Vertex v1 = myGraph.FindSpecifedVertex(scanner.nextInt());
                Vertex v2 = myGraph.FindSpecifedVertex(scanner.nextInt());
                
                Integer edgeWeight = scanner.nextInt();
                
                // Populate the Adjacencies lists.
                v1.AddNeighbor(v2, edgeWeight);
                v2.AddNeighbor(v1, edgeWeight);

            }
        }

        //myGraph.PerformDijsktraAlgorithm(myGraph.GetSourceVertex(), numberOfVertices);
        myGraph.PerformBellmanFordAlgorithm(myGraph.GetSourceVertex(), myGraph.GetNumberOfVertices(), bellmanFordPw);
        myGraph.PerformFloydWarshallAlogrithm(myGraph.GetNumberOfVertices(), floydWarshallPw);

        scanner.close();
        bellmanFordPw.close();
        floydWarshallPw.close();
    }

    public Integer GetNumberOfVertices()
    {
        return this.numberOfVertices;
    }

    public void SetNumberOfVertices(Integer numberOfVertices) 
    {
        this.numberOfVertices = numberOfVertices; 
    }

    public Integer GetNumberOfEdges()
    {
        return this.numberOfEdges;
    }

    public void SetNumberOfEdges(Integer numberOfEdges)
    {
        this.numberOfEdges = numberOfEdges;
    }

    public ArrayList<Vertex> GetVerticesList()
    {
        return this.vertices;
    }

    public void AddVertice(Vertex newVertice)
    {
        this.vertices.add(newVertice);
    }

    public Vertex GetSourceVertex()
    {
        return this.source;
    }
    public void SetSourceVertex(Vertex source)
    {
        this.source = source;
    }

    /*  Takes an integer for the number of vertices.
        Returns a Intialized matrix for FloydWarshall algorithm. */
    private Integer[][] SetupMatrix(Integer numberOfVertices)
    {
        Integer newMatrix[][] = new Integer[numberOfVertices][numberOfVertices];
        // Arbitrary Inifnity needed due to overflow using Integer.MAX_VALUE
        int arbitraryInfinity = 1000000;

        // Initialize every slot in the matrix to an arbitrary infinity.
        for(int i = 0; i < numberOfVertices; i++)
        {
            for(int j = 0; j < numberOfVertices; j++)
            {
                newMatrix[i][j] = arbitraryInfinity;
            }   
        }
        
        // Fill slots in the matrix with what is known, (v, v) = 0 and (u, v) = weight.
        for (Vertex currentVertex : this.vertices) 
        {
            Integer v = currentVertex.GetElement() - 1;
            newMatrix[v][v] = 0;

            for (Vertex neighborVertex : currentVertex.GetNeighborConnections().keySet()) 
            {
                Integer weight = currentVertex.GetNeighborConnections().get(neighborVertex);

                newMatrix[v][(neighborVertex.GetElement() - 1)] = weight;              
            }
        }

        return newMatrix;
    }

    /*  Takes an Integer for the number of vertices, and a printWriter to output FloydWarshell file */
    public void PerformFloydWarshallAlogrithm(Integer numberOfVertices, PrintWriter pw)
    {
        Integer matrix[][] = SetupMatrix(numberOfVertices);
        Integer k, a, b;

        // O(V^3) 
        for(k = 0; k < numberOfVertices; k++)
        {
            for(a = 0; a < numberOfVertices; a++)
            {
                for(b = 0; b < numberOfVertices; b++)
                {
                    int calculatedDistance = matrix[a][k] + matrix[k][b];

                    if(matrix[a][b] > calculatedDistance)
                    {
                        matrix[a][b] = matrix[a][k] + matrix[k][b];
                    }
                }
            }
        }

        FloydOutput(matrix, numberOfVertices, pw);
    }

    /*  Takes a completed FloydWarshall Integer Matrix, the number of vertices and a PrintWriter.
        Then outputs each index of the matrix to the specifed PrintWriter. */
    private void FloydOutput(Integer[][] distanceMatrix, Integer numberOfVertices ,PrintWriter pw)
    {
        pw.println(numberOfVertices);

        for(int i = 0; i < numberOfVertices; i++)
        {
            for(int j = 0; j < numberOfVertices; j++)
            {
                pw.print(distanceMatrix[i][j] + " ");
            }

            pw.println();
        }
    }

    /*  Takes a Vertex that represents the intial Vertex, and an Integer for the number of Vertices
        in the graph. */
    public void PerformBellmanFordAlgorithm(Vertex source, Integer numberOfVertices, PrintWriter pw)
    {
        // Intialize every vertex distance and parent to Infinity and null.
        for (Vertex vertex : this.vertices) 
        {
            vertex.SetCurrentDistance(Integer.MAX_VALUE);
            vertex.setCurrentParent(null);
        }


        // Intialize the source.
        source.SetCurrentDistance(0);
        source.setCurrentParent(null);

        // Iterate over every edge  (V - 1) times.
        for(int i = 0; i < (numberOfVertices - 1); i++)
        {
            for (Vertex vertex : this.vertices) 
            {
                // Allows for iterating over the connected edges vertices.
                for (Vertex v : vertex.GetNeighborConnections().keySet()) 
                {
                    Integer calculatedDistance = vertex.GetCurrentDistance() + vertex.GetNeighborConnections().get(v);

                    if(vertex.GetCurrentDistance() != Integer.MAX_VALUE)
                    {
                        if(calculatedDistance < v.GetCurrentDistance())
                        {
                            v.SetCurrentDistance(calculatedDistance);
                            v.setCurrentParent(vertex);
                        }

                    }
                }
            }  
        }

        // Detects a negative cycle.
        for (Vertex vertex : this.vertices) 
        {
            for (Vertex v : vertex.GetNeighborConnections().keySet()) 
            {
                Integer calculatedDistance = vertex.GetCurrentDistance() + vertex.GetNeighborConnections().get(v);
                if(vertex.GetCurrentDistance() != Integer.MAX_VALUE)
                {        
                    if(calculatedDistance < v.GetCurrentDistance())
                    {
                        System.out.print("There exists a negative cycle.");
                        break;
                    }   
                }
            }
        }

        BellmanOutput(pw);
    }

    /* Takes a PrintWriter to write to then prints for Assignment 3 format. */
    private void BellmanOutput(PrintWriter pw)
    {
        pw.println(numberOfVertices);

        for (Vertex vert : this.vertices) 
        {
            if (vert.currentParent != null)
                pw.println(vert.GetElement() + " " + vert.GetCurrentDistance() + " " + vert.GetCurrentParent().GetElement());
            else
                pw.println(vert.GetElement() + " " + vert.GetCurrentDistance() + " 0");
        }
    }

    /*  Takes an Initial Vertex and the number of vertices then performs Dijkstras. */
    public void PerformDijsktraAlgorithm(Vertex source, Integer numOfVert) 
    {
        // Intialize source node.
        source.SetCurrentDistance(0);
        source.setCurrentParent(null);
        PriorityQueue<Vertex> pQueue = new PriorityQueue<Vertex>(numOfVert, new Vertex());
        pQueue.add(source);

        while(!pQueue.isEmpty())
        {
            Vertex currentPlace = pQueue.poll();
            
            for (Vertex vertex : currentPlace.GetNeighborConnections().keySet()) 
            {
                Integer calculatedDistance = currentPlace.GetCurrentDistance() + currentPlace.GetNeighborConnections().get(vertex);
                
                if(!vertex.visited)
                {
                    // If the new current distance is shorter then the current distance, replace the vertex's values with the optimal values
                    // ... then re add it to the priority queue to reorganize.  
                    if(calculatedDistance < vertex.GetCurrentDistance())
                    {            
                        
                        pQueue.remove(vertex);
                        vertex.SetCurrentDistance(calculatedDistance);
                        vertex.setCurrentParent(currentPlace);
                        pQueue.add(vertex); 
                    }

                }

            }
            
            currentPlace.visited = true;
        }

        
    }

    // Return the Vertex of the specified element.
    public Vertex FindSpecifedVertex(Integer element) 
    {
        return this.vertices.get((element - 1));
    }

    // Generate a list of Vertices and store their element in ascending order (1, 2, 3...)
    public void CreateVertices(Integer numberOfVertices, Integer src) 
    {
        for (int i = 1; i <= numberOfVertices; i++) 
        {
            Vertex tempVert = new Vertex(i);

            if (i == src)
                this.source = tempVert;

            this.vertices.add(tempVert);
        }
    }

    public class Vertex implements Comparator<Vertex>
    {
        private Integer element;
        private Integer currentDistance;
        private Vertex currentParent;
        private boolean visited;
        private HashMap<Vertex, Integer> neighborConnections;

        public Vertex() 
        {

        }

        public Vertex(Integer element)
        {
            this.element = element;
            currentDistance = Integer.MAX_VALUE;
            neighborConnections = new HashMap<Vertex, Integer>();
            currentParent = null;
        }

        public Integer GetElement() { return this.element; }

        public Integer GetCurrentDistance() { return this.currentDistance; }
        public void SetCurrentDistance(Integer currentDistance) { this.currentDistance = currentDistance; } 

        public Vertex GetCurrentParent() { return this.currentParent; }
        public void setCurrentParent(Vertex currentParent) { this.currentParent = currentParent; }

        public HashMap<Vertex, Integer> GetNeighborConnections() { return this.neighborConnections; }
        public void AddNeighbor(Vertex targetVertex, Integer edgeWeight)
        {
            this.neighborConnections.put(targetVertex, edgeWeight);
        }

        public void RemoveNeighbor(Vertex targeVertex)
        {
            this.neighborConnections.remove(targeVertex);
        }

        @Override
        public int compare(Vertex firstVertex, Vertex secondVertex)
        {
            return (firstVertex.GetCurrentDistance() - secondVertex.GetCurrentDistance());
        }

    }

}