/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rappresentazioneconoscenza;




import java.lang.StringBuilder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import static org.apache.jena.vocabulary.RDFS.Literal;




/**
 *
 * @author Martin
 */
public class SparqlQuery {
    
     static final String inputWine  = "C:\\Users\\marti_000\\Documents\\NetBeansProjects\\rappresentazioneConoscenza\\src\\main\\java\\com\\mycompany\\rappresentazioneconoscenza\\ontology\\wine.owl";
     static final String wineURI = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#";
     
     public static List findWineInstanceSparqlQuery(String sugar, String color, String flavor, String body) {
        List<String> instances = new ArrayList<String>();
        Model modelWine = ModelFactory.createDefaultModel();
        // use the FileManager to open the bloggers RDF graph from the filesystem
        InputStream in = FileManager.get().open(inputWine);
        if (in == null) {
        throw new IllegalArgumentException( "File: " + inputWine + " not found");
        }
        // read the RDF/XML file
        modelWine.read( in, "" );  
       
        String queryString = "PREFIX wine: <http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#>"+
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
            "SELECT ?subject ?object WHERE { \n?subject wine:hasSugar wine:"+ sugar +".\n"+
                                              "?subject wine:hasColor wine:"+ color +".\n"+
                                              "?subject wine:hasFlavor wine:"+ flavor +".\n"+
                                              "?subject wine:hasBody wine:"+ body +".}";

                 //"?i wine:hasFlavor wine:Strong .}";
        String[] lines = queryString.split("\n");
        
        if (sugar == null) {
            for(int i=0;i<lines.length;i++){
                if(lines[i].equals("?subject wine:hasSugar wine:"+ sugar +".")){
                    lines[i]="";
                }
            }
        }
        
        if (color == null){
            for(int i=0;i<lines.length;i++){
                if(lines[i].equals("?subject wine:hasColor wine:"+ color +".")){
                    lines[i]="";
                }
            }
        }
        
        if (flavor == null) {
            for(int i=0;i<lines.length;i++){
                if(lines[i].equals("?subject wine:hasFlavor wine:"+ flavor +".")){
                    lines[i]="";
                }
            }
        }
        
        if (body == null) {
            for(int i=0;i<lines.length;i++){
                if(lines[i].equals("?subject wine:hasBody wine:"+ body +".}")){
                    lines[i]="}";
                }
            }
        }
        
        StringBuilder finalStringBuilder = new StringBuilder();
        for(String s:lines){
           if(!s.equals("")){
               finalStringBuilder.append(s).append(System.getProperty("line.separator"));
            }
        }
        String finalQuery = finalStringBuilder.toString();
        System.out.println(finalQuery);
         
        Query query = QueryFactory.create(finalQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, modelWine)) {
            ResultSet results = qexec.execSelect();
             // Output query results
            //ResultSetFormatter.out(System.out, results, query);
            Iterator<QuerySolution> res = results;
            Resource resource = null;
            for ( ; res.hasNext() ; )
                {
                    QuerySolution soln = res.next();
                    resource = soln.getResource("?subject");
                    String answer = resource.getLocalName();
                    //System.out.println(answer);
                    instances.add(answer);
                }

             return instances;
        }
         
     };
   
    /*
    public static void main(String[] args) {
    

        Model modelWine = ModelFactory.createDefaultModel();
        // use the FileManager to open the bloggers RDF graph from the filesystem
        InputStream in = FileManager.get().open(inputWine);
        if (in == null) {
        throw new IllegalArgumentException( "File: " + inputWine + " not found");
        }
        // read the RDF/XML file
        modelWine.read( in, "" );  
       
         String queryString = "PREFIX wine: <http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#>PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>PREFIX owl: <http://www.w3.org/2002/07/owl#>PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>SELECT ?subject ?object WHERE { \n" +
        "?subject wine:hasFlavor wine:Strong.\n" +
        "}\n" +
        " ";
                                  //"  \n?subject wine:hasSugar wine:Dry.\n"+
                                  //"?subject wine:hasColor ?object. }";
         
                                
        String[] lines = queryString.split("\n");
         for(int i=0;i<lines.length;i++){
            if(lines[i].equals("?subject wine:hasBody wine:Full.")){
                lines[i]="";
            }
        }
         
        StringBuilder finalStringBuilder = new StringBuilder();
        for(String s:lines){
           if(!s.equals("")){
               finalStringBuilder.append(s).append(System.getProperty("line.separator"));
            }
        }
        String test = finalStringBuilder.toString();
         System.out.println(test);
        

                 //"?i wine:hasFlavor wine:Strong .}";
 
         Query query = QueryFactory.create(queryString);
         try (QueryExecution qexec = QueryExecutionFactory.create(query, modelWine)) {

            ResultSet results = qexec.execSelect();
             
             // Output query results
             ResultSetFormatter.out(System.out, results, query);
         }

    }*/
   
}
