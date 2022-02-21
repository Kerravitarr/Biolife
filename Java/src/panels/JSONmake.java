package panels;


import java.util.ArrayList;

public class JSONmake {
	private class JSONParametr{
		public JSONParametr(String key, String value) {
			this.key = key;
			this.value = value;
		}
		String key;
		String value;
		public String toString() {
			return "\"" + key + "\":" + value;
		}
	}
	
	ArrayList<JSONParametr> parametrs;
	
	public JSONmake(){
		parametrs = new ArrayList<>();
	}
	
	public void add(String key, String value) {
		parametrs.add(new JSONParametr(key,"\"" + value.replaceAll("\"", "\\\\\"") + "\""));
	}
	public void add(String key, int value) {
		add(key,(long) value);
	}
	public void add(String key, long value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	public void add(String key, double value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	public void add(String key, String[] value) {
		String str = "[";
		for(int i = 0 ; i < value.length ; i ++) {
			if(i > 0) str +=",";
			str += "\"" + value[i].replaceAll("\"", "\\\\\"") + "\"";
		}
		str += "]";
		parametrs.add(new JSONParametr(key,str));
	}
	public void add(String key, JSONmake value) {
		parametrs.add(new JSONParametr(key,value.toJSONString()));
	}
	public void add(String key, boolean value) {
		parametrs.add(new JSONParametr(key,value ? "true" : "false"));
	}
	public void add(String key, int[] value) {
		String str = "[";
		for(int i = 0 ; i < value.length ; i ++) {
			if(i > 0) str +=",";
			str += value[i];
		}
		str += "]";
		parametrs.add(new JSONParametr(key,str));
	}
	public void add(String key, JSONmake[] value) {
		String str = "[";
		for(int i = 0 ; i < value.length ; i ++) {
			if(i > 0) str +=",";
			str += value[i].toJSONString();
		}
		str += "]";
		parametrs.add(new JSONParametr(key,str));
	}
	
	public String toJSONString() {
		String jsonStr = "{";
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) jsonStr +=",";
			jsonStr += parametrs.get(i).toString();
		}
		jsonStr += "}";
		return jsonStr;
	}


	public void replace(String key, String value) {
		for(JSONParametr i : parametrs) {
			if(i.key.equals(key)) {
				i.value = "\"" + value.replaceAll("\"", "\\\\\"") + "\"";
				break;
			}
		}
	}

	public String toFormatJSONString() {
		String ret = "";
		
		int countTab = 0;
		boolean StartBlosk = false;
		for(byte i : toJSONString().getBytes()) {
			if(i == '{' || i == '[' ) {
				if(StartBlosk)
					ret += '\n' + getTab(countTab);
				ret += ((char)i);
				countTab++;
				StartBlosk = true;
			} else if(i == '}'|| i == ']') {
				countTab--;
				if(StartBlosk)
					ret += ((char)i);
				else
					ret += '\n' + getTab(countTab) + ((char)i);
				StartBlosk = false;
			}else if(i == ',') {
				if(StartBlosk)
					ret += '\n' + getTab(countTab);
				ret += ((char)i);
				ret += '\n' + getTab(countTab);
				StartBlosk = false;
			} else {
				if(StartBlosk)
					ret += '\n' + getTab(countTab);
				ret += ((char)i);
				StartBlosk = false;
			}
		}
		
		return ret;
	}

	private String getTab(int countTab) {
		String tabs = "";
		for (int i = 0; i < countTab; i++) {
			tabs += '\t';
		}
		return tabs;
	}



}
