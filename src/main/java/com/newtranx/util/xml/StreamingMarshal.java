/*
 * Copyright 2016 NewTranx Co. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newtranx.util.xml;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class StreamingMarshal<T> implements Closeable, AutoCloseable {

	protected XMLStreamWriter xmlOut;

	protected Marshaller marshaller;

	private final Class<T> type;

	public StreamingMarshal(Class<T> type) throws JAXBException {
		this.type = type;
		JAXBContext context = JAXBContext.newInstance(type);
		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
	}

	public void open(OutputStream output) throws XMLStreamException, IOException, JAXBException {
		xmlOut = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
		xmlOut.writeStartDocument();
		xmlOut.writeStartElement(rootElemTag());
		writeHeader();
	}

	public void write(T t) throws JAXBException {
		this.write(t, type.getSimpleName());
	}

	public void write(T t, String qname) throws JAXBException {
		JAXBElement<T> element = new JAXBElement<T>(QName.valueOf(qname), type, t);
		marshaller.marshal(element, xmlOut);
	}

	public void close() {
		try {
			writeFooter();
			xmlOut.writeEndDocument();
			xmlOut.close();
		} catch (XMLStreamException | JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract String rootElemTag();

	protected void writeHeader() throws JAXBException, XMLStreamException {

	}

	protected void writeFooter() throws JAXBException, XMLStreamException {

	}

}
