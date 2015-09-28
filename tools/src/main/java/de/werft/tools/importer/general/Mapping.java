package de.werft.tools.importer.general;

// TODO: Auto-generated Javadoc
/**
 * The Class Mapping.
 * This resembles a mapping from an input tag to it's according ontology class/property/literal.
 * We provide the mapping to give you the possibility to model your ontology accordingly to your input.
 * A Mapping without a context is just a name mapping. A mapping with context is a special mapping for ambigious definitions.
 * As example if you have several tags named name and different ontology mappings like projectName and so on, you can provide a context
 * in which the rule applies.<br>
 * This class is immutable.
 */
public class Mapping {

	/**  The input, an xml tag for example. */
	private String input;
	
	/**
	 *  The input XML tag additionally must have a certain attribute
	 */
	private String inputAttributeName;

	/**
	 *  The input XML tag additionally must have a certain attribute with a certain value
	 */
	private String inputAttributeValue;
	
	/** A super node of the input. 
	 * Example: 'company' would be the parent of it's attribute 'name' */
	private String context;
	
	/**
	 * How many levels away is the context node. If not given direct parent is assumed.
	 * Example in dramaqueen document: <Frame ..> Level 1 --> <children> / <Frame ..> Level 2 --> <Scene ..>
	 */
	private int distance;
	
	/** The action. */
	/* 3 actions are provided link, map, contextMap */
	private MappingAction action;
	
	/**  The name of the equivalent node in the ontology. */
	private String property;
	
	/** The link node. */
	private String linkNode;
	
	/** The direct back link. */
	private boolean directBackLink;
	
	/**
	 * Instantiates a new link -maybe context aware- mapping.
	 * If you switch the linking method to direct backlink the mapping will only
	 * be linked back towards the next parent node in a direct path.
	 * The context denotes here to the xml parent.
	 *
	 * @param input the input - xml
	 * @param context the context - xml
	 * @param action the action
	 * @param property the property - ontology
	 * @param linkNode the link node - ontology
	 * @param directBackLink true if you want direct back linking
	 */
	private Mapping(String input, MappingAction action, String context,
			String linkNode, String property, boolean directBackLink) {
		this.input = input;
		this.context = context;
		this.action = action;
		this.property = property;
		this.linkNode = linkNode;
		this.directBackLink = directBackLink;
	}
	
	private Mapping(MappingAction action, String input, String inputAttributeName, String inputAttributeValue,
			String context, int distance, String ontologyTarget) {
		
		this.action = action;
		this.input = input;
		this.inputAttributeName = inputAttributeName;
		this.inputAttributeValue = inputAttributeValue;
		this.context = context;
		this.distance = distance;
		this.property = ontologyTarget;
	}
	
	public static Mapping createMapping(MappingAction action, String input, String inputAttributeName,
			String inputAttributeValue, String context, int distance, String ontologyTarget) {
		return new Mapping(action,input,inputAttributeName,inputAttributeValue,context,distance,ontologyTarget);
	}

	/**
	 * Instantiates a new simple mapping or a convert action.
	 *
	 * @param input the input - xml
	 * @param action the action
	 * @param property the property - ontology
	 */
	public static Mapping createMapping(String input, MappingAction action, String property) {
		return new Mapping(input, action, "", "", property, false);
	}
	
	/**
	 * Instantiates a new context aware mapping.
	 *
	 * @param input the input - xml
	 * @param action the action
	 * @param context the context - xml
	 * @param property the property - ontology
	 */
	public static Mapping createMapping(String input, MappingAction action, String context, String property) {
		return new Mapping(input, action, context, "", property, false);
	}
	
	/**
	 * Instantiates a new link -maybe context aware- mapping.
	 *
	 * @param input the input - xml
	 * @param action the action 
	 * @param context the context - xml
	 * @param linkNode the link node - ontology
	 * @param property the property - ontology
	 */
	public static Mapping createMapping(String input, MappingAction action, String context, String linkNode, String property) {
		return new Mapping(input, action, context, linkNode, property, false);
	}
	
	/**
	 * Instantiates a new link -maybe context aware- mapping.
	 * If you switch the linking method to direct backlink the mapping will only
	 * be linked back towards the next parent node in a direct path.
	 * The context denotes here to the xml parent.
	 *
	 * @param input the input - xml
	 * @param context the context - xml
	 * @param action the action
	 * @param property the property - ontology
	 * @param linkNode the link node - ontology
	 * @param directBackLink true if you want direct back linking
	 */
	public static Mapping createMapping(String input, MappingAction action, String context, String linkNode, String property, boolean directBackLink) {
		return new Mapping(input, action, context, linkNode, property, directBackLink);
	}
	
	/**
	 * The Enum MappingAction. <br>
	 * The Actions are MAP, LINK, CONTEXTMAP. <br>
	 * Use the appropriate {@link Mapping} constructor.<br>
	 * The context is always the parent node. 
	 * So if you have node without childrens and attributes corresponding to a ontology class and you
	 * want to map some attributes with context, the context is the parent node not the actual one.
	 */
	public enum MappingAction {
		
		/** A simple map operation.
		 * We use the simple map for classes (they have no context awareness) and as fallback or default
		 * for all other cases, except linkin. */
		MAP,
		
		/** The link map operation.
		 * Here you provide a link mapping to which nodes in the result model we link our actual node.
		 * As example we can say on node "x" LINK under context "y" to node "z" with the following property "hasX".<br>
		 * Please note that you can switch from backlinking to all nodes to the first direct node only. */
		LINK, 
		
		/** The context aware map operation. 
		 * This is evaluated before the simple map operation. Because of that you can only provide context aware or simple mappings,
		 * never both at the same time. The context aware map operation is not used for class mappings. <br>
		 * As example one can say on node "x" map under context "y" to "z".<br> */
		CONTEXTMAP,
		
		/** The convert action. 
		 * This action triggers that from a tag the subgraph down everything is collected as giant literal. <br>
		 * As example you can think of it as convert from tag "x" every child as string and use the property "y" 
		 * to store it. */
		CONVERT
	}
	
	/**
	 * Gets the input.
	 *
	 * @return the input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	public String getOutput() {
		return property;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + (directBackLink ? 1231 : 1237);
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result
				+ ((linkNode == null) ? 0 : linkNode.hashCode());
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mapping other = (Mapping) obj;
		if (action != other.action)
			return false;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (directBackLink != other.directBackLink)
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (linkNode == null) {
			if (other.linkNode != null)
				return false;
		} else if (!linkNode.equals(other.linkNode))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Mapping [input=" + input + ", context=" + context + ", action="
				+ action + ", property=" + property + ", linkNode=" + linkNode
				+ ", directBackLink=" + directBackLink + "]";
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public MappingAction getAction() {
		return action;
	}

	/**
	 * Gets the link node.
	 *
	 * @return the link node
	 */
	public String getLinkNode() {
		return linkNode;
	}

	public boolean isDirectBackLink() {
		return directBackLink;
	}

	public int getDistance() {
		return distance;
	}

	public String getInputAttributeName() {
		return inputAttributeName;
	}

	public String getInputAttributeValue() {
		return inputAttributeValue;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
