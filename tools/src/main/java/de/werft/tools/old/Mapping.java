package de.werft.tools.old;

import org.apache.commons.lang.StringUtils;

/**
 * This class represents an old mapping and it knows
 * how to transform itself into a partial rml mapping.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
class Mapping {

    private String path = "";

    private String condAttr = "";

    private String condAttrValue = "";

    private String source = "";

    private String sourceName = "";

    private String targetClass = "";

    private String targetProperty = "";

    private String targetType = "";

    private String rmlPart = "";

    Mapping() {
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    String getCondAttr() {
        return condAttr;
    }

    void setCondAttr(String condAttr) {
        this.condAttr = condAttr;
    }

    String getCondAttrValue() {
        return condAttrValue;
    }

    void setCondAttrValue(String condAttrValue) {
        this.condAttrValue = condAttrValue;
    }

    String getSource() {
        return source;
    }

    void setSource(String source) {
        this.source = source;
    }

    String getSourceName() {
        return sourceName;
    }

    void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    String getTargetClass() {
        return targetClass;
    }

    void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    String getTargetProperty() {
        return targetProperty;
    }

    void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    String getTargetType() {
        return targetType;
    }

    void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    String getRmlPart() {
        return rmlPart;
    }

    @Override
    public String toString() {
        return "Mapping{\n" +
                "path=" + path + '\n' +
                ", condAttr=" + condAttr + '\n' +
                ", condAttrValue=" + condAttrValue + '\n' +
                ", source=" + source + '\n' +
                ", sourceName=" + sourceName + '\n' +
                ", targetClass=" + targetClass + '\n' +
                ", targetProperty=" + targetProperty + '\n' +
                ", targetType=" + targetType + '\n' +
                "}\n" +
                "rmlPart:\n" +
                rmlPart + "\n";
    }

    Mapping createMapping(String chunk) {
        Mapping mapping = new Mapping();
        String split[] = chunk.split("\n");

        for (String part : split) {

            switch (StringUtils.substringBetween(part, ".", "=")) {
                case "xmlNodePath":
                    mapping.setPath(StringUtils.substringAfter(part, "="));
                    break;

                case "conditionalAttributeName":
                    mapping.setCondAttr(StringUtils.substringAfter(part, "="));
                    break;

                case "conditionalAttributeValue":
                    mapping.setCondAttrValue(StringUtils.substringAfter(part, "="));
                    break;

                case "contentSource":
                    mapping.setSource(StringUtils.substringAfter(part, "="));
                    break;

                case "contentElementName":
                    mapping.setSourceName(StringUtils.substringAfter(part, "="));
                    break;

                case "targetOntologyClass":
                    mapping.setTargetClass(StringUtils.substringAfter(part, "="));
                    break;

                case "targetOntologyProperty":
                    mapping.setTargetProperty(StringUtils.substringAfter(part, "="));
                    break;

                case "targetPropertyType":
                    mapping.setTargetType(StringUtils.substringAfter(part, "="));
                    break;

                default:
                    System.out.println("No valid mapping pendant found for " + part);
            }
        }
        return mapping;
    }


    void transform(boolean nextClassOrLast) {
        StringBuilder builder = new StringBuilder();

        /* transform class mappings */
        if (isClassMapping()) {
            builder.append("<#").append(StringUtils.substringAfterLast(targetClass, "/")).append(">\n")
                    .append("\trml:logicalSource [\n\t\trml:iterator \"")
                    .append(path).append("\";\n\t\trml:referenceFormulation ql:XPath;\n")
                    .append("\t];\n\n")
                    .append("\trr:subjectMap [\n")
                    .append("\t\trr:template \"").append(targetClass).append("/{@id}\";\n")
                    .append("\t\trr:class <").append(targetClass).append(">;\n");

            if (nextClassOrLast) {
                builder.append("\t].\n\n");
            } else {
                builder.append("\t];\n\n");
            }

        /* transform node content to predicate map */
        } else if (isNodeProperty()) {
            builder.append("\trr:predicateObjectMap [\n")
                    .append("\t\trr:predicate \"").append(targetProperty).append("\";\n")
                    .append("\t\trr:objectMap [ rml:reference \"").append(path).append("\"; ]\n");

            if (nextClassOrLast) {
                builder.append("\t].\n\n");
            } else {
                builder.append("\t];\n\n");
            }
            /* transform attribute content to predicate map */
        } else if (isAttrProperty()) {
            builder.append("\trr:predicateObjectMap [\n")
                    .append("\t\trr:predicate \"").append(targetProperty).append("\";\n")
                    .append("\t\trr:objectMap [ rml:reference \"@").append(sourceName).append("\"; ]\n");

            if (nextClassOrLast) {
                builder.append("\t].\n\n");
            } else {
                builder.append("\t];\n\n");
            }
        }
        this.rmlPart = builder.toString();
    }

    boolean isAttrProperty() {
        return !path.isEmpty() && "ATTRIBUTE".equalsIgnoreCase(source) && !targetClass.isEmpty()
                && !targetProperty.isEmpty() && "DATATYPE_PROPERTY".equalsIgnoreCase(targetType)
                && !sourceName.isEmpty() && condAttrValue.isEmpty() && condAttr.isEmpty();
    }

    boolean isNodeProperty() {
        return !path.isEmpty() && "TEXT_CONTENT".equalsIgnoreCase(source) && !targetClass.isEmpty()
                && !targetProperty.isEmpty() && "DATATYPE_PROPERTY".equalsIgnoreCase(targetType)
                && condAttr.isEmpty() && condAttrValue.isEmpty() && sourceName.isEmpty();
    }

    /* a old class mapping has only a path and a target class */
    boolean isClassMapping() {
        return !path.isEmpty() && !targetClass.isEmpty()
                && condAttr.isEmpty() && condAttrValue.isEmpty() && source.isEmpty()
                && sourceName.isEmpty() && targetProperty.isEmpty() && targetType.isEmpty();
    }

    void shortenPath(String path) {
        this.path = StringUtils.substringAfter(this.path, path + "/");
    }

}
