package com.ggktech.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ggktech.dao.PropertiesFileReader;

/**
 * Class which contains method for sending mail.
 */
public class SendMail {

	private static final Logger LOGGER = Logger.getLogger(SendMail.class);

	private Properties configProperties;
	static String filePathTestSuite;
	String path = null;
	private boolean isConsolidated = false;

	public boolean getIsConsolidated() {
		return isConsolidated;
	}

	public void setIsConsolidated(boolean isConsolidated) {
		this.isConsolidated = isConsolidated;
	}

	/**
	 * @function : sendMail(String reportFileName)
	 * @returns : None
	 * @parameter : reportFileName : Name of the file to be sent in mail
	 */
	public void sendMail(Map<String, ExecutionResult> executionResult, String toEmailAddress) {
		try {
			configProperties = PropertiesFileReader.getInstance().getPropFile("Config.properties");
			filePathTestSuite = configProperties.getProperty("filePathTestSuite");
			String[] toemail = toEmailAddress.split(",");
			String[] cc = {};
			String[] bcc = {};
			execute(true, toemail, cc, bcc, executionResult);
		} catch (Exception e) {
			LOGGER.error("Exception in sendMail method" + e);
		}

	}

	/**
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @parameter : reportFileName : Name of the file to be sent in mail
	 * @parameter : userName : Username of the sender
	 * @parameter : passWord : Password of the sender
	 * @parameter : host : host of the mail
	 * @parameter : port : port from which mail should be send
	 * @parameter : starttls : starttls setting
	 * @parameter : auth : Auth setting
	 * @parameter : debug : debug setting
	 * @parameter : socketFactoryClass : socket factory class setting
	 * @parameter : fallback : fallback setting
	 * @parameter : to : To whom mail should be sent
	 * @parameter : cc : cc
	 * @parameter : bcc : bcc
	 * @parameter : subject : subject of the mail
	 * @parameter : text : text of the mail
	 * @parameter : attachmentPath : attachment to the mail
	 * @parameter : attachmentName : attachment name
	 * @returns : true/false based on status of the mail
	 */
	private void execute(boolean debug, String[] to, String[] cc, String[] bcc,
			Map<String, ExecutionResult> executionResultMap)
			throws MessagingException, ParserConfigurationException, SAXException, IOException {
		String userName = configProperties.getProperty("fromMail");
		String passWord = configProperties.getProperty("password");
		String host = configProperties.getProperty("host");
		String port = configProperties.getProperty("port");
		String starttls = configProperties.getProperty("starttls");
		String auth = configProperties.getProperty("auth");
		String socketFactoryClass = configProperties.getProperty("socketFactoryClass");
		String fallback = configProperties.getProperty("fallback");
		String subject = configProperties.getProperty("subject");
		String text = configProperties.getProperty("text");
		String attachmentName = configProperties.getProperty("consolidatedEmailFileName");
		String consolidEmailablePath = configProperties.getProperty("consolidatedEmailablePath");
		path = ConfigConstants.PARENTFOLDER_PATH;
		StringBuilder emailbuilder = new StringBuilder();
		emailbuilder.append(
				"<p><font face='verdana' size='2'>Hello, \n</font></p><p><font face='verdana' size='2'> Following is the summary of Execution \n </font></p>");
		emailbuilder.append("<html><body><table border='1' style='border-collapse:collapse;'>");
		emailbuilder.append("<tr>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Name" + "</b></font></th>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Browser" + "</b></font></th>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Environment" + "</b></font></th>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Tests" + "</b></font></th>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Passed" + "</b></font></th>");
		emailbuilder.append("<th style='background-color:Cyan' col width='130'><font face='verdana' size='2'><b>"
				+ "Failed" + "</b></font></th>");
		emailbuilder.append("</tr>");

		int failedCount = 0;

		for (Entry<String, ExecutionResult> entry : executionResultMap.entrySet()) {
			String[] seperatedName = entry.getKey().split("\\|");
			emailbuilder.append("<tr>");
			emailbuilder.append("<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
					+ seperatedName[0] + "</b></font></td>");
			emailbuilder.append("<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
					+ seperatedName[1] + "</b></font></td>");
			emailbuilder.append("<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
					+ seperatedName[2] + "</b></font></td>");
			emailbuilder.append("<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
					+ entry.getValue().getTcCount() + "</b></font></td>");
			emailbuilder.append("<td style='color:green' align='center'><font face='verdana' size='2'><b>"
					+ entry.getValue().getTcPassCount() + "</b></font></td>");
			emailbuilder.append("<td style='color:red' align='center'><font face='verdana' size='2'><b>"
					+ entry.getValue().getTcFailCount() + "</b></font></td>");
			emailbuilder.append("</tr>");
			if (entry.getValue().getTcFailCount() > 0) {
				failedCount = 1;
			}
		}
		emailbuilder.append("</table></body></html>");
		emailbuilder.append("<p></p>");

		/** Logic to create body for failed test scripts */
		if (failedCount > 0) {
			emailbuilder.append("<h3 align='center'>Failed Scenarios \n</h3>");
			emailbuilder.append("<html><body><table border='1' style='border-collapse:collapse;'>");
			emailbuilder.append("<tr>");
			emailbuilder.append("<th style='background-color:Cyan' width='10%'><font face='verdana' size='2'><b>"
					+ "ModuleName" + "</b></font></th>");
			emailbuilder.append("<th style='background-color:Cyan' width='10%'><font face='verdana' size='2'><b>"
					+ "Browser" + "</b></font></th>");
			emailbuilder.append("<th style='background-color:Cyan' width='10%'><font face='verdana' size='2'><b>"
					+ "Environment" + "</b></font></th>");
			emailbuilder.append("<th style='background-color:Cyan' width='20%'><font face='verdana' size='2'><b>"
					+ "TestScript" + "</b></font></th>");
			emailbuilder.append("</tr>");

			for (Entry<String, ExecutionResult> entry : executionResultMap.entrySet()) {
				if (entry.getValue().getFailScript() != null) {
					String[] testScriptName = entry.getValue().getFailScript().split(",");

					// Remove duplicates
					Set<String> setTestScriptNames = Arrays.asList(testScriptName).stream().collect(Collectors.toSet());
					String[] seperatedName = entry.getKey().split("\\|");
					for (String tcName : setTestScriptNames) {
						if ("".equals(tcName)) {
							break;
						}
						emailbuilder.append("<tr>");
						emailbuilder.append(
								"<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
										+ seperatedName[0] + "</b></font></td>");
						emailbuilder.append(
								"<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
										+ seperatedName[1] + "</b></font></td>");
						emailbuilder.append(
								"<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
										+ seperatedName[2] + "</b></font></td>");
						emailbuilder.append(
								"<td style='background-color:white' align='center'><font face='verdana' size='2'><b>"
										+ tcName + "</b></font></td>");
						emailbuilder.append("</tr>");
					}
				}
			}
			emailbuilder.append("</table></body></html>");

		}

		emailbuilder.append("<p><font face='verdana' size='2'>\n Thanks</font></p>");
		// Object Instantiation of a properties file.
		Properties props = new Properties();
		props.put("mail.smtp.user", userName);
		props.put("mail.smtp.host", host);

		if (!"".equals(port)) {
			props.put("mail.smtp.port", port);
		}

		if (!"".equals(starttls)) {
			props.put("mail.smtp.starttls.enable", starttls);
			props.put("mail.smtp.auth", auth);
		}

		if (debug) {
			props.put("mail.smtp.debug", "true");
		} else {
			props.put("mail.smtp.debug", "false");
		}

		if (!"".equals(port)) {
			props.put("mail.smtp.socketFactory.port", port);
		}
		if (!"".equals(socketFactoryClass)) {
			props.put("mail.smtp.socketFactory.class", socketFactoryClass);
		}
		if (!"".equals(fallback)) {
			props.put("mail.smtp.socketFactory.fallback", fallback);
		}

		Session session = Session.getDefaultInstance(props, null);
		MimeMessage msg = new MimeMessage(session);
		msg.setText(text);
		msg.setSubject(subject);
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		MimeBodyPart emailBodyPart = new MimeBodyPart();
		File srcFolder = new File(path + consolidEmailablePath);
		List<File> fileList = new ArrayList<>();
		getAllFiles(srcFolder, fileList);
		writeZipFile(srcFolder, fileList);
		DataSource source = new FileDataSource(path + "\\" + attachmentName);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(attachmentName);
		emailBodyPart.setContent(emailbuilder.toString(), "text/html");
		multipart.addBodyPart(emailBodyPart);
		multipart.addBodyPart(messageBodyPart);
		msg.setContent(multipart);
		msg.setFrom(new InternetAddress(userName));

		for (int i = 0; i < to.length; i++) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
		}
		for (int i = 0; i < cc.length; i++) {
			msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
		}
		for (int i = 0; i < bcc.length; i++) {
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
		}
		msg.saveChanges();
		Transport tranport = session.getTransport("smtp");
		tranport.connect(host, userName, passWord);
		tranport.sendMessage(msg, msg.getAllRecipients());
		tranport.close();

	}

	/**
	 * Method to get all files in a directory.
	 * 
	 * @param dir
	 * @param fileList
	 */
	private void getAllFiles(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		List<String> emailableConsolidatedFilters = Arrays
				.asList(configProperties.getProperty("emailableConsolidatedFilters").split(","));
		emailableConsolidatedFilters.forEach(String::trim);

		List<String> emailableModulewiseFilters = Arrays
				.asList(configProperties.getProperty("emailableModulewiseFilters").split(","));
		emailableModulewiseFilters.forEach(String::trim);

		for (File file : files) {
			if (!file.isHidden()) {
				if (!getIsConsolidated()) {
					if (!emailableModulewiseFilters.contains(file.getName())) {
						fileList.add(file);
						if (file.isDirectory()) {
							getAllFiles(file, fileList);
						}
					}
				} else {
					if (!emailableConsolidatedFilters.contains(file.getName())) {
						fileList.add(file);
						if (file.isDirectory()) {
							getAllFiles(file, fileList);
						}
					}
				}
			}
		}
	}

	/**
	 * @param directoryToZip
	 *            : location to emailable path
	 * @param fileList
	 *            : list of files in selected folder
	 */
	private void writeZipFile(File directoryToZip, List<File> fileList) {
		try {
			FileOutputStream fos = new FileOutputStream(directoryToZip.getName() + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (File file : fileList) {
				if (!file.isDirectory()) {
					addToZip(directoryToZip, file, zos);
				}
			}
			zos.close();
			fos.close();

		} catch (Exception e) {
			LOGGER.info("Exception in the writeZipFile() " + e);
		}
	}

	/**
	 * @param directoryToZip
	 *            : location to emailable path
	 * @param file
	 *            : list of files in selected folder
	 * @param zos
	 *            : ZipOutputStream
	 * @throws IOException
	 */
	private void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws IOException {

		FileInputStream fis = new FileInputStream(file);

		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		zos.closeEntry();
		fis.close();
	}
}
