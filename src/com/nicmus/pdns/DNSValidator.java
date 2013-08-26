/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 * 
 * Control panel for PowerDNS (http://powerdns.com)
 * Copyright (C) 2010 Jivko Sabev
 * Jivko Sabev (jivko.sabev@gmail.com) jsabev@nicmus.com
 * 
 * @author jsabev 
 */
package com.nicmus.pdns;

import java.util.regex.Pattern;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;

import com.nicmus.pdns.dao.RecordDAO;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.Record.Type;



/**
 * 
 * validate DNS Records
 * Credit for regular expressions goes to powerADMIN author Rejo Zenger 
 * (rejo@zegner.nl)
 * 
 */
@Name("dnsValidator")
@AutoCreate
public class DNSValidator {
	
	//This monstrosity came from:
	//http://www.schlitt.net/spf/tests/spf_record_regexp-03.txt
	private static final Pattern SPF_PATTERN = Pattern.compile(
			"^[Vv]=[Ss][Pp][Ff]1( +([-+?~]?([Aa][Ll][Ll]|[Ii][Nn][Cc][Ll][Uu][Dd]" +
			"[Ee]:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*" +
			"[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|" +
			"12[0-8])?[Rr]?[+-/=_]*\\})|[Aa](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]" +
			"?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))?" +
			"((/([1-9]|1[0-9]|2[0-9]|3[0-2]))?(//([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?)?" +
			"|[Mm][Xx](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%" +
			"\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))?" +
			"((/([1-9]|1[0-9]|2[0-9]|3[0-2]))?(//([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?)?|[Pp]" +
			"[Tt][Rr](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|" +
			"%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))" +
			"?|[Ii][Pp]4:([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|" +
			"1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(/([1-9]|1[0-9]|2[0-9]|3[0-2]))" +
			"?|[Ii][Pp]6:(::|([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|([0-9A-Fa-f]{1,4}:){1,8}:|" +
			"([0-9A-Fa-f]{1,4}:){7}:[0-9A-Fa-f]{1,4}|([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}){1,2}|" +
			"([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,3}|([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4})" +
			"{1,4}|([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,5}|([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]" +
			"{1,4}){1,6}|[0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,7}|:(:[0-9A-Fa-f]{1,4}){1,8}|([0-9A-Fa-f]" +
			"{1,4}:){6}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4]" +
			"[0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}" +
			"|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){6}:([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){5}:([0-9A-Fa-f]{1,4}:)?" +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|" +
			"([0-9A-Fa-f]{1,4}:){4}:([0-9A-Fa-f]{1,4}:){0,2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){3}:([0-9A-Fa-f]{1,4}:){0,3}([0-9]|" +
			"[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|" +
			"([0-9A-Fa-f]{1,4}:){2}:([0-9A-Fa-f]{1,4}:){0,4}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|[0-9A-Fa-f]{1,4}::([0-9A-Fa-f]{1,4}:)" +
			"{0,5}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|::" +
			"([0-9A-Fa-f]{1,4}:){0,6}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))" +
			"(/([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?|" +
			"[Ee][Xx][Ii][Ss][Tt][Ss]:(%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))|[Rr][Ee][Dd][Ii][Rr][Ee][Cc][Tt]=" +
			"(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|" +
			"%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\})" +
			"|[Ee][Xx][Pp]=(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]" +
			"|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]" +
			"([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\})|[A-Za-z]" +
			"[-.0-9A-Z_a-z]*=(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|" +
			"11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*))* *$");
	
	@In
	private StatusMessages statusMessages;
	
	@In(create=true)
	private RecordDAO recordDAO;
	
	/**
	 * Validate that the given record is valid within the list of records in the 
	 * zone
	 * @param record The record to validate
	 * @param domainId - the domainId of the DNS zone the record belongs to
	 * @return true on successful validation, false otherwise
	 */
	public boolean isValid(Record record, int domainId){

		//see if the record exists
		if(this.recordDAO.recordExists(record.getName(), record.getContent(), record.getType(), domainId)){
			this.statusMessages.addFromResourceBundle(Severity.WARN, "RecordAction.RecordExists", record.getName());
			return false;
		}

		
		switch(record.getType()){
			case A:{
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidIPV4(record.getContent())) return false;
				if(this.recordDAO.recordNameExists(record.getName(), Type.CNAME, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_Exists", record.getName());
					return false;
				}
				return true;
			}
			case AAAA: {
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidIPV6(record.getContent())) return false;
				if(this.recordDAO.recordNameExists(record.getName(), Type.CNAME, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_Exists", record.getName());
					return false;
				}
				return true;
			}
			case CNAME: {
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidFQDN(record.getContent())) return false;
				if(this.recordDAO.recordContentExists(record.getName(), Type.NS, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_MX_NS");
					return false;
				}
				if(this.recordDAO.recordContentExists(record.getName(), Type.MX, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_MX_NS");
					return false;
				}
				if(this.recordDAO.recordNameExists(record.getName(), Type.A, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_A", record.getName());				
					return false;
				}
				if(this.recordDAO.recordNameExists(record.getName(), Type.AAAA, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_A", record.getName());				
					return false;
				}
				if(this.recordDAO.recordNameExists(record.getName(), Type.CNAME, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_A", record.getName());				
					return false;
				}

				return true;
			}
			case MX:{
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidFQDN(record.getContent())) return false;
				if(this.recordDAO.recordContentExists(record.getContent(), Type.CNAME, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_Exists", record.getName());
					return false;
				}
				return true;
			}
			case NS: {
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidFQDN(record.getContent())) return false;
				if(this.recordDAO.recordContentExists(record.getContent(), Type.CNAME, domainId)){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.DNS_CNAME_Exists", record.getName());
					return false;
				}
				return true;
			}
			case PTR: {
				if(!this.isValidFQDN(record.getName())) return false;
				if(!this.isValidFQDN(record.getContent())) return false;
				return true;
			}
			case TXT: {
				if(!this.isPrintable(record.getName())) return false;
				if(!this.isPrintable(record.getContent())) return false;
				record.setName(this.quoteRecordContent(record.getName()));
				record.setContent(this.quoteRecordContent(record.getContent()));
				return true;
			}
			case SPF: {
				if(!this.isValidSPF(record.getContent())) return false;
				record.setName(this.quoteRecordContent(record.getName()));
				record.setContent(this.quoteRecordContent(record.getContent()));
				return true;
			}
			default:
				return true;
		}
	}

	/**
	 * 
	 * @param hostname
	 * @return
	 */
	public boolean isValidFQDN(String hostname){
		boolean valid = true;
		if(hostname.length() > 255){
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.HostNameTooLong");
			valid = false;
		}
		if(hostname.indexOf('.') == -1){
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidHostName", hostname);
			valid = false;
		}
		String[] parts= hostname.split("\\.");
		int position = 0;
		for(String part : parts){
			if(position++ == 0){
				//we are at the first hostname part
				//could start with '*'
				if(!part.matches("^(\\*|[\\w-]+)$")){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidCharacter", part);
					valid = false;
				}
			} else {
				//no '*' is allowed
				if(!part.matches("[\\w-]+$")){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidCharacter", part);
					valid = false;
				}
			}
			//starts or ends in '-';
			if(part.startsWith("-") || part.endsWith("-")){
				this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidCharacter2", "-", part);
				valid = false;
			}
			if(part.length() < 1){
				return false;
			}
			
			if(part.length() > 63){
				this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.HostPartTooLong", part);
				valid = false;
			}
		}
		return valid;
	}
	
	/**
	 * Validate IPV4
	 * @param ip the ip address
	 * @return true on valid ipv4 address, false otherwise
	 */
	public boolean isValidIPV4(String ip){
		boolean isValid = true;
		if(!ip.matches("^[0-9\\.]{7,15}$")){
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidFormat",ip);
			isValid = false;
		}
		
		String[] ipQuads = ip.split("\\.");
		if(ipQuads != null && ipQuads.length !=4){
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidIP", ip);
			isValid = false;
		}
		
		if(ipQuads != null){
			for(String quad : ipQuads){
				try {
					int parseInt = Integer.parseInt(quad);
					if(parseInt > 255){
						this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidIPQuad", quad);
						isValid = false;
					}
				} catch (NumberFormatException e){
					this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidCharacter", quad);
					isValid = false;
				}
			}
		} else {
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidIP", ip);
			isValid = false;
		}
		
		return isValid;
	}
	

	/**
	 * 
	 * @param ip
	 * @return
	 */
	public boolean isValidIPV6(String ip){
		boolean isValid = true;
		if(!ip.matches("(?i)^[0-9a-f]{0,4}:([0-9a-f]{0,4}:){0,6}[0-9a-f]{0,4}$")){
			isValid = false;
			this.statusMessages.addFromResourceBundle(Severity.INFO, "DNSValidator.InvalidIPV6Address", ip);
		}
		
		String[] hexParts = ip.split(":");
		if(hexParts == null){
			isValid = false;
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidIPV6Address2", ip);
			return isValid;
		}
		
		if(hexParts.length > 8 || hexParts.length < 3){
			isValid = false;
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidHexPart", hexParts.length);
		}
		
		int emptyHexParts = 0;
		for(String hexPart : hexParts){
			if(hexPart.isEmpty()){
				emptyHexParts++;
			}
		}
		
		if(emptyHexParts == 0 && hexParts.length != 8){
			isValid = false;
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.InvalidFormat", ip);
		}
		
		return isValid;
	}
		
	/**
	 * 
	 * @param string
	 * @return
	 */
	public boolean isPrintable(String string){
		boolean pritable = string.matches("^[\\p{Print}]+$");
		if(!pritable){
			this.statusMessages.addFromResourceBundle(Severity.ERROR, "DNSValidator.NonPrintable");
		}
		return pritable;
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public boolean isValidSPF(String content){
		if(SPF_PATTERN.matcher(content).matches()){
			return true;
		}
		this.statusMessages.addFromResourceBundle(Severity.ERROR, "Invalid SRV target.");
		return false;
	}

	/**
	 * Enclose the record content in quotation marks if there are spaces found. 
	 * Applicable for TXT and SRV records
	 * @param text 
	 * @return 
	 */
	private String quoteRecordContent(String text){
		if(text != null){
			if(text.split("\\s+").length > 1 && !(text.startsWith("\"") && text.endsWith("\""))){
				text = "\"" + text + "\"";
			}
		}
		return text;
	}

	
}
