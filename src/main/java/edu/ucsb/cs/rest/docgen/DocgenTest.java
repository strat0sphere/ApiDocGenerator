package edu.ucsb.cs.rest.docgen;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import edu.ucsb.cs.rest.api.API;
import edu.ucsb.cs.rest.api.Field;
import edu.ucsb.cs.rest.api.Header;
import edu.ucsb.cs.rest.api.NamedInputBinding;
import edu.ucsb.cs.rest.api.NamedTypeDef;
import edu.ucsb.cs.rest.api.Operation;
import edu.ucsb.cs.rest.api.Parameter;
import edu.ucsb.cs.rest.api.Resource;
import edu.ucsb.cs.rest.parser.APIDescriptionParser;

import java.util.logging.Logger;



public class DocgenTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//API api = APIDescriptionParser.parse("{\"name\" : \"Foo\"}"); 
		API api = APIDescriptionParser.parseFromFile("input/starbucks.json");
		//System.out.println(api.getName());
		PrintWriter out = new PrintWriter(new FileWriter("output/index.html"));
		
		Velocity.init();
		
		try
		{
			Template resourceTemplate = Velocity.getTemplate("velocity_templates/resource.vm");
			Template operationTemplate = Velocity.getTemplate("velocity_templates/operation.vm");
			
			String resourceName, path = "";
			NamedInputBinding[] namedInputBindings;
			NamedTypeDef[] namedTypeDefs = api.getDataTypes();
			
			Writer writer = new StringWriter();
			for (Resource resource : api.getResources())
			{
				resourceName = resource.getName();
				path = resource.getPath();
				namedInputBindings = resource.getInputBindings();
				
				VelocityContext resourceContext = new VelocityContext();
				
				resourceContext.put("resourceName", resourceName);
				resourceContext.put("path", path);
				resourceContext.put("inputBindings", namedInputBindings);

				resourceTemplate.merge(resourceContext, writer);
				
				
				//Print list of operations
				for (Operation operation : resource.getOperations())
				{
					VelocityContext operationContext = new VelocityContext();
					
					operationContext.put("operationName", operation.getName());
					operationContext.put("resourceName", resourceName);
					operationContext.put("description", operation.getDescription());
					operationContext.put("path", path);
					operationContext.put("errors", operation.getErrors());
					
					/*
					 for(edu.ucsb.cs.rest.api.Error error : operation.getErrors())
					 
					{
						System.out.println(error.getCause() + error.getStatus());
					}
					*/
					operationContext.put("httpMethod", operation.getMethod());
					operationContext.put("input", operation.getInput());
					
					try
					{
						//1. Search if there is defined a type for the input and if it matches to a defined Data type
						if(operation.getInput().getType() != null)
						{
							for (NamedTypeDef namedTypeDef: namedTypeDefs)
							{
								if (namedTypeDef.getName().equals(operation.getInput().getType()))
								{
									System.out.println("Name: " + namedTypeDef.getName());
									
									for (Field field : namedTypeDef.getFields())
									{
										System.out.println("Field Name: " + field.getName());
										System.out.println("Field Description: " + field.getDescription());
										System.out.println("Field Type: " + field.getType());
										System.out.println("Field Ref: " +  field.getRef());
									}
	
								}
							}
						}
						//2. Search if there are parameters and if they match with the bindings specified by the resource
						if (operation.getInput().getParams() != null)
						{
							for (Parameter param : operation.getInput().getParams())
							{
								for (NamedInputBinding inputBinding: namedInputBindings)
								{
									if (inputBinding.getId().equals(param.getBinding()))
									{
										System.out.println("Name: " + inputBinding.getName() + " - Type:" + inputBinding.getType());
										//TO ADD: Add to operation context a field for each parameter
									}
								}
								System.out.println(param.getBinding());
								System.out.println(param.getBinding().getClass().getSimpleName());
							}
						}
						
					}
					catch (NullPointerException npe)
					{
						//Do nothing
					}
					operationContext.put("output", operation.getOutput());
					
					
					operationTemplate.merge(operationContext, writer);
					
				}
				
			}
			
			out.print(writer);
			out.close();
			//System.out.println(writer);
		}
		catch (ResourceNotFoundException rnfe)
		{
			//couldn't find the template
		}
		catch (ParseErrorException pee)
		{
			// syntax error : problem parsing the template
		}
		catch (MethodInvocationException mie)
		{
			// Sth invoked in the template threw an exception
		}
		
	}

}
