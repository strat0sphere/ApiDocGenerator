package edu.ucsb.cs.rest.docgen;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import edu.ucsb.cs.rest.api.API;
import edu.ucsb.cs.rest.api.Operation;
import edu.ucsb.cs.rest.api.Resource;
import edu.ucsb.cs.rest.parser.APIDescriptionParser;
//import org.apache.velocity.tools.generic.RenderTool;


public class DocgenTest {

	/*
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    //Instead of using default, pass in a decoder. 
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
		*/
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//API api = APIDescriptionParser.parse("{\"name\" : \"Foo\"}"); 
		API api = APIDescriptionParser.parseFromFile("input/starbucks.json");
		System.out.println(api.getName());
		Velocity.init();
		
		try
		{
			Template resourceTemplate = Velocity.getTemplate("velocity_templates/resource.vm");
			Template operationTemplate = Velocity.getTemplate("velocity_templates/operation.vm");
			
			String resourceName, path = "";
			
			Writer writer = new StringWriter();
			for (Resource resource : api.getResources())
			{
				resourceName = resource.getName();
				path = resource.getPath();
				VelocityContext resourceContext = new VelocityContext();
				resourceContext.put("resourceName", resourceName);
				resourceContext.put("path", path);
				resourceTemplate.merge(resourceContext, writer);
				
				//Print list of operations
				for (Operation operation : resource.getOperations())
				{
					VelocityContext operationContext = new VelocityContext();
					operationContext.put("operationName", operation.getName());
					operationContext.put("resourceName", resourceName);
					operationContext.put("description", operation.getDescription());
					operationContext.put("path", path);
					//Print list of errors
					operationContext.put("input", operation.getInput());
					operationContext.put("httpmethod", operation.getMethod());
					operationContext.put("output", operation.getOutput());
					operationTemplate.merge(operationContext, writer);
				}
				
			}
			
			System.out.println(writer);
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
