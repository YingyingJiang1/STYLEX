package org.example.style;

import org.dom4j.Element;
import org.example.parser.common.MyParser;

import java.util.*;


public class ProgramStyle implements DomIO {
    Map<String, Style> styleMap = new HashMap<>();


    @Override
    public void addElement(Element root, MyParser parser) {
        for (Style style : styleMap.values()) {
            if (style instanceof CommonStyle commonStyle) {
                commonStyle.addElement(root, parser);
            }

        }
    }

    @Override
    public void parseElement(Element root, MyParser parser) {
        for (Style style : styleMap.values()) {
            if (style instanceof CommonStyle commonStyle) {
                commonStyle.parseElement(root, parser);
            }
        }
        
    }

    public void add(Style style) {
        styleMap.put(style.getStyleName(), style);
    }

    public List<Style> getStyles() {
        return styleMap.values().stream().toList();
    }

    public Style getStyle(String styleName) {
        return styleMap.get(styleName);
    }
}
