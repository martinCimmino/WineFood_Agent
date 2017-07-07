/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rappresentazioneconoscenza;


import com.mycompany.rappresentazioneconoscenza.Utils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.apache.jena.enhanced.BuiltinPersonalities.model;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;


/**
 *
 * @author Martin
 */
public class wineFoodAgent extends Object {
    /**
        NOTE that the file is loaded from the class-path and so requires that
        the data-directory, as well as the directory containing the compiled
        class, must be added to the class-path when running this and
        subsequent examples.
    */    
    static final String inputWine  = "C:\\Users\\marti_000\\Documents\\NetBeansProjects\\rappresentazioneConoscenza\\src\\main\\java\\com\\mycompany\\rappresentazioneconoscenza\\ontology\\wine.owl";
    static final String inputFood  = "C:\\Users\\marti_000\\Documents\\NetBeansProjects\\rappresentazioneConoscenza\\src\\main\\java\\com\\mycompany\\rappresentazioneconoscenza\\ontology\\food.owl";  
    static final String wineURI = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#";
    static final String foodURI = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/food#";
    static final String excludedMenuFoodClassName = "WineGrape";
    
    // variable for GUI event handlers
    static String [] dishInstancesArr ;
    static List dishInstancesList;
    static OntModel ontoModelFood;
    static OntModel ontoModelWine;
    static OntResource selectedDish;
    
    // methods for Gui event handlers
    public static String[] getDishInstancesArr(){
        return dishInstancesArr;
    }
    /*
     * This method creates the ontology model and retrieves the food instances 
     * ( also transofrms instances from list to array )
     */
    public void InitializeAgent () {
        ontoModelFood = Utils.createOntoModel(inputFood); // passing inputWine is the same  
        // Get dish instances, need inference model in order to get all instances
        dishInstancesList = Utils.getInstancesOfClass(ontoModelFood, "EdibleThing", foodURI, excludedMenuFoodClassName); 
        dishInstancesArr = Utils.populateArrayFromList((ArrayList<OntResource>) dishInstancesList);
    }
    
    /*
     * This method creates find the wine properties for a given instance food:
     * - First find the instance food Resource from the given String parameter
     * - Second retrieve the food instance Meal Course class from the class of the food instance
     * - Third retrieve the wine properties expressed inside the Meal Course Class
     */
    public Map lookUpWineProperties (String dishSelectedString) {
        for (Iterator it = dishInstancesList.iterator(); it.hasNext();) {
            OntResource instance = (OntResource) it.next();
            if (dishSelectedString.equals(instance.getLocalName())){
                System.out.println("Trovato piatto selezionato: " + instance.getLocalName());
                selectedDish = instance;
            } 
        }
        // Get the class of the dish instance
        System.out.println(selectedDish.getLocalName());
        Individual selectedDishIndividual = selectedDish.asIndividual();
        
        // in case user select "ThompsonSeedless" we need to get the SweetFruit SuperClass
        OntClass selectedDishClass = null;
        if("ThompsonSeedless".equals(selectedDish.getLocalName())){
            selectedDishClass = selectedDishIndividual.getOntClass(true).getSuperClass().getSuperClass();
        }else{
            selectedDishClass = selectedDishIndividual.getOntClass(true);
            System.out.println("Class: " + selectedDishClass.getLocalName());
        }

        OntClass selectedDishClassCourse = Utils.getMealCourseFromMealOntClass(ontoModelFood, selectedDishClass);
        
        //System.out.println("Let's go find properties that wine need to have within this MealCourse: " + selectedDishClassCourse.getLocalName());
        
        Map dictionary = Utils.getPropertiesWineOfSelectedDishClassCourse(ontoModelFood, selectedDishClassCourse);
        //System.out.println(dictionary);
        return dictionary;  
    }
    
    /*
     * This method calls a Sparql Query in order to retrieve the wine instance that satisfy given properties
     * N.B string parameters must be null if property is not present
     */
    public String [] lookupWine (String sugar_Prop, String color_Prop, String flavor_Prop, String body_Prop){
        List wineInstances = SparqlQuery.findWineInstanceSparqlQuery(sugar_Prop, color_Prop, flavor_Prop, body_Prop);
        String [] wine = (String[]) wineInstances.toArray(new String[0]);
        return wine;
    }
    
    
    /*
    public static void main (String args[]) {    
        // need just one
        //OntModel ontoModelWine = Utils.createOntoModel(inputWine);
        ontoModelFood = Utils.createOntoModel(inputFood);
        
        
        // Get dish instances, need inference model in order to get all instances
        dishInstancesList = Utils.getInstancesOfClass(ontoModelFood, "EdibleThing", foodURI, excludedMenuFoodClassName);    
        dishInstancesArr = Utils.populateArrayFromList((ArrayList<OntResource>) dishInstancesList);
        
        // Get the dish we selected from the list
        for (Iterator it = dishInstancesList.iterator(); it.hasNext();) {
            OntResource instance = (OntResource) it.next();
            if ("FraDiavolo".equals(instance.getLocalName())){
                System.out.println("Trovato piatto selezionato: " + instance.getLocalName());
                selectedDish = instance;
            } 
        }
        // Get the class of the dish instance
        System.out.println(selectedDish.getLocalName());
        Individual selectedDishIndividual = selectedDish.asIndividual();
        
        // in case user select "ThompsonSeedless" we need to get the SweetFruit SuperClass
        OntClass selectedDishClass = null;
        if("ThompsonSeedless".equals(selectedDish.getLocalName())){
            selectedDishClass = selectedDishIndividual.getOntClass(true).getSuperClass().getSuperClass();
        }else{
            selectedDishClass = selectedDishIndividual.getOntClass(true);
            System.out.println("Class: " + selectedDishClass.getLocalName());
        }

        OntClass selectedDishClassCourse = Utils.getMealCourseFromMealOntClass(ontoModelFood, selectedDishClass);
        
        System.out.println("Let's go find properties that wine need to have within this MealCourse: " + selectedDishClassCourse.getLocalName());
        
        Map dictionary = Utils.getPropertiesWineOfSelectedDishClassCourse(ontoModelFood, selectedDishClassCourse);
        System.out.println(dictionary);
        
        List wineInstances = SparqlQuery.findWineInstanceSparqlQuery(null, null, "Strong", null);
        String wine = null;
        for (Iterator iter = wineInstances.iterator(); iter.hasNext(); )
        {
            wine = (String) iter.next();
            System.out.println("Found this wine: " + wine );
        }  
    } */

}
    

