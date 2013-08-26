package com.nicmus.pdns.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.jboss.seam.annotations.Name;

/**
 * Used to store the information of user->guid for password resetting options.
 * A link is sent to the user with the guid saved here. when the user clicks 
 * on the link, it is verified against that table.
 * @author jsabev
 *
 */
@Name("passwordResetToken")
@Entity
public class PasswordResetToken implements Serializable{
	private static final long serialVersionUID = -6525602365865952554L;
	private int id;
	private String userName;
	private String guid;
	private Date dateCreated;
	private Date expiryDate;
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getId() {
		return this.id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return this.userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return this.guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the dateCreated
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateCreated() {
		return this.dateCreated;
	}
	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	/**
	 * @return the expiryDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Index(name="expiryDateIdx")
	public Date getExpiryDate() {
		return this.expiryDate;
	}
	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	@PrePersist
	public void onCreate(){
		//set the expiry date in 24 hours
		Calendar cal = Calendar.getInstance();
		this.setDateCreated(cal.getTime());
		cal.add(Calendar.DATE,1);
		this.setExpiryDate(cal.getTime());
	}
	
	
}
