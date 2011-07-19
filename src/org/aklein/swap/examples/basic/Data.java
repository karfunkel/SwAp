package org.aklein.swap.examples.basic;

import com.jgoodies.binding.beans.Model;

public class Data extends Model
{

	private static final long serialVersionUID = 1353986289633118578L;
	private String vorname = "";
	private String nachname = "";
	private String strasse = "";
	private String plz = "";
	private String ort = "";
	private String tel = "";
	private String fax = "";

	public Data()
	{}

	public Data(String vorname, String nachname, String strasse, String plz, String ort, String tel, String fax)
	{
		super();
		this.vorname = vorname;
		this.nachname = nachname;
		this.strasse = strasse;
		this.plz = plz;
		this.ort = ort;
		this.tel = tel;
		this.fax = fax;
	}

	public String getVorname()
	{
		return vorname;
	}

	public void setVorname(String vorname)
	{
		Object old = this.vorname;
		this.vorname = vorname;
		firePropertyChange("vorname", old, vorname);
	}

	public String getNachname()
	{
		return nachname;
	}

	public void setNachname(String nachname)
	{
		Object old = this.nachname;
		this.nachname = nachname;
		firePropertyChange("nachname", old, nachname);
	}

	public String getStrasse()
	{
		return strasse;
	}

	public void setStrasse(String strasse)
	{
		Object old = this.strasse;
		this.strasse = strasse;
		firePropertyChange("strasse", old, strasse);
	}

	public String getPlz()
	{
		return plz;
	}

	public void setPlz(String plz)
	{
		Object old = this.plz;
		this.plz = plz;
		firePropertyChange("plz", old, plz);
	}

	public String getOrt()
	{
		return ort;
	}

	public void setOrt(String ort)
	{
		Object old = this.ort;
		this.ort = ort;
		firePropertyChange("ort", old, ort);
	}

	public String getTel()
	{
		return tel;
	}

	public void setTel(String tel)
	{
		Object old = this.tel;
		this.tel = tel;
		firePropertyChange("tel", old, tel);
	}

	public String getFax()
	{
		return fax;
	}

	public void setFax(String fax)
	{
		Object old = this.fax;
		this.fax = fax;
		firePropertyChange("fax", old, fax);
	}
}
