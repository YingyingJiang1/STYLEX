package org.example.style;

import org.dom4j.Element;
import org.example.parser.common.MyParser;


public interface DomIO {
  void addElement(Element parent, MyParser parser);
  void parseElement(Element parent, MyParser parser);

}
