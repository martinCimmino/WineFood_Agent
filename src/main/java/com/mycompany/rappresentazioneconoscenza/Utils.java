/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rappresentazioneconoscenza;

import static com.mycompany.rappresentazioneconoscenza.wineFoodAgent.foodURI;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;

import org.apache.jena.shared.JenaException;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;



/**
 *
 * @author Martin
 */
public class Utils {
    
        public static OntModel createOntoModel (String Url) {
            
            OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, null);
            try 
            {
                InputStream in = FileManager.get().open(Url);
                try 
                {
                    ontoModel.read(in, null);
                } 
                catch (Exception e) 
                {
                    System.err.println(e);
                }
                System.out.println("Ontology " + Url + " loaded.");
            } 
            catch (JenaException je) 
            {
                System.err.println("ERROR" + je.getMessage());
                System.exit(0);
            }
            return ontoModel;

        }

        public static void printStatements(OntModel m, Resource s, Property p, Resource o) {
            for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
                Statement stmt = i.nextStatement();
                    System.out.println(" - " + PrintUtil.print(stmt));            
                }
        }
        
        public static String getRestrictionBlankNodeOnPropertyHasFood(OntModel m, Resource s, Property p, Resource o) {
            String res = null;
            OntProperty hasFood = m.getOntProperty(foodURI + "hasFood");
            for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
                    Statement stmt = i.nextStatement();
                    if(stmt.getSubject().hasProperty(OWL.onProperty, hasFood)){
                        System.out.println("hasFood predicate found on all values from " + o.getLocalName() );
                        //System.out.println("intersection identifier:" + stmt.getSubject().toString());
                        res = stmt.getSubject().toString();
                        break;
                    }
                }
            return res;          
        }
        
        public static Map getPropertiesWineOfSelectedDishClassCourse(OntModel m, OntClass selectedDishClassCourse) {
            Map<String, String> dict = new HashMap<String, String>();
            OntProperty hasDrink = m.getOntProperty(foodURI + "hasDrink");
            Property value;
            for (Iterator<OntClass>  i = selectedDishClassCourse.listSuperClasses(true); i.hasNext(); ) {
                OntClass sup = i.next();                
                if (sup.isRestriction()){ 
                    if(sup.asRestriction().onProperty(hasDrink) ){
                        //array.add(sup.getSameAs().getId());
                        Restriction restriction = sup.asRestriction();
                        //System.out.println("restriction: " + restriction.getOnProperty().getLocalName());
                        if(restriction.isAllValuesFromRestriction()){
                            Resource restriction_avf_resource = restriction.asAllValuesFromRestriction().getAllValuesFrom();  
                            for (StmtIterator props = restriction_avf_resource.listProperties(OWL.onProperty); props.hasNext();) {
                                Statement st = props.next();
                                OntProperty key = m.getOntProperty(st.getObject().toString());
                                if(!dict.containsKey(key.getLocalName()) ){
                                    try {
                                        value = m.getProperty(restriction_avf_resource.getProperty(OWL.hasValue).getObject().toString());
                                    } catch (Exception e){
                                        value = m.getProperty(" Moderate"); // Strong alternatively. NonSpicyRedMeat 
                                    }
                                    dict.put(key.getLocalName(), value.getLocalName() );
                                    System.out.println("Within this meal course wine for predicate :" + key.getLocalName() + " has value: "+ value.getLocalName());
                                }
                            }              
                        }
                    } 
                }
            }
            return dict;
        }
              
        public static List getInstancesOfClass(OntModel ontoModel, String ID, String URI , String excludedClass) {
            List<OntResource> instances = new ArrayList<OntResource>();
            int counter = 0;
            OntClass artefact = ontoModel.getOntClass( URI + ID );
            for (Iterator<? extends OntResource> i = artefact.listInstances(); i.hasNext(); ) {
                OntResource c = i.next();
                if(!c.asIndividual().getOntClass(true).getLocalName().equals(excludedClass)){
                    instances.add(c);
                    counter++;
                    //System.out.println( "Instance: " + c.getLocalName() );
                }
              }
            System.out.println( "Instance dishes total number: " + counter);
            return instances;
        }
        
        public static String[] populateArrayFromList(ArrayList<OntResource> arrayList)
        {
           String[] arr = new String[arrayList.size()];
           for (int i = 0; i < arrayList.size(); i++)
           {
              arr[i] = arrayList.get(i).getLocalName();
           }
           return arr;
        }
        
        /*
         * This method exploit the fact Meal Course Classes has Restriction with "all values from" (on property has food) a given food class 
         *
         */
        public static OntClass getMealCourseFromMealOntClass (OntModel ontoModelFood, OntClass selectedDishClass){
            String restriction = getRestrictionBlankNodeOnPropertyHasFood(ontoModelFood, null , OWL.allValuesFrom , selectedDishClass );
            OntClass selectedDishClassCourse = null;
            OntClass mealCourse = ontoModelFood.getOntClass(foodURI + "MealCourse");
            for (Iterator<OntClass> subclasses = mealCourse.listSubClasses(true); subclasses.hasNext(); ){
                OntClass sub = subclasses.next();
                // Second we look for each subclass of MealCourse if they have as superClasses the restriction hasFood-allValuesFrom(selectedDishClass)
                // N.B we look up on direct linking, if failing try listSuperClasses(false)
                for (ExtendedIterator<OntClass> superClasses = sub.listSuperClasses(false); superClasses.hasNext(); ) {
                    OntClass sup = superClasses.next();
                    if(sup.isRestriction()){
                        if(sup.toString().equals(restriction)){
                            System.out.println("Found! " + sub.getLocalName() + " has restriction: on property hasFood / all values from " + selectedDishClass.getLocalName());
                            selectedDishClassCourse = sub;
                            break;
                        }         
                    } 
                }
            }
            return selectedDishClassCourse;
        };
         
         
        
       
        /* 
        public static void printSubClassesOfClass(OntModel ontoModel, String ID, String URI) {
            OntClass artefact = ontoModel.getOntClass( URI + ID );
            for (Iterator<OntClass> i = artefact.listSubClasses(true); i.hasNext(); ) {
                OntClass c = i.next();
                System.out.println("Has SubClass: " + c.getURI() );
              }
        }
        
        public static void printIntersections(OntModel ontoModel, String property) {
            for (ExtendedIterator<IntersectionClass> i = ontoModel.listIntersectionClasses(); i.hasNext(); ) {
                IntersectionClass c = i.next();
          
                System.out.println( "Restriction all values from get resource: " + c.asResource());
                
                
              }
        }
        public static void printRestrictions(OntModel ontoModel, String property) {
            for (ExtendedIterator<Restriction> i = ontoModel.listRestrictions(); i.hasNext(); ) {
                Restriction c = i.next();
                if(c.getOnProperty().getLocalName().equals(property)){
                //AllValuesFromRestriction alc = c.asAllValuesFromRestriction();
                
                System.out.println( "Restriction all values from get resource: " + c.getRDFType().getLocalName());
                }
                
              }
        }
        
        public static void printPropertiesOfClass(OntModel ontoModel, String ID, String URI) {
            OntClass artefact = ontoModel.getOntClass( URI + ID );
            for (StmtIterator i = artefact.listProperties(); i.hasNext(); ) {
                Statement c = i.next();
                System.out.println( "Instance: " + c.getPredicate() );
              }
        }
        */

}
