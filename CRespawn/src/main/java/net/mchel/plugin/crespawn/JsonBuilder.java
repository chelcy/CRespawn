package net.mchel.plugin.crespawn;

import java.util.List;

import org.bukkit.ChatColor;

/*
 *  Copyright (C) 2015 Gabriel POTTER (gpotter2)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
//from https://github.com/gpotter2/TitleSender/blob/master/src/fr/cabricraft/titlesender/TitleSender.java
public class JsonBuilder {

	public static enum JSONParam {
		BOLD,
		ITALIC,
		UNDERLINED,
		STRIKETHROUGH,
		OBFUSCATED;
	}

	/**
	 * If it is used tellraw
	 * @author chelcy
	 *
	 */
	public static enum JSONClickEvent {
		RUN_COMMAND,
		SUGGEST_COMMAND,
		OPEN_URL,
		CHANGE_PAGE
	}

	/**
	 * A util to create JSON messages.
	 *
	 * @author gpotter2
	 *
	 */
	public static class JSONPart {
		ChatColor color;
		String string;
		boolean bold = false;
		boolean italic = false;
		boolean underlined = false;
		boolean strikethrough = false;
		boolean obfuscated = false;
		String clickevent = "null";
		String clickeventvalue = "null";
		String hoverevent = "null";
		String hovereventvalue = "null";

		/**
		 * A util to create a JSON part message, with a text and color.
		 *
		 * @author gpotter2
		 *
		 */
		public JSONPart(String string, ChatColor color){
			if(string == null){
				new NullPointerException("The string cannot be null !").printStackTrace();
				return;
			}
			this.string = string.replaceAll("'", "").replaceAll('"'+"", "");
			if(color != null){
				this.color = color;
			} else {
				color = ChatColor.WHITE;
			}
		}
		public String getString(){
			return string;
		}
		public ChatColor getColor(){
			return color;
		}
		public String getJSONPart(){
			return "{text:'" + string + "',color:'" + color.name().toLowerCase() + "',bold:" + bold + ",italic:" + italic + ",underlined:" + underlined
					+ ",strikethrough:" + strikethrough + ",obfuscated:" + obfuscated + ",clickEvent:{action:'" + clickevent + "',value:'" + clickeventvalue + "'}"
					+ ",hoverEvent:{action:'" + hoverevent + "',value:" + hovereventvalue + "}"+ "}";
		}
		public String __INVALID__getJSONPartExtra(){
			return "{text:'" + string + "',color:'" + color.name().toLowerCase() + "',bold:" + bold + ",italic:" + italic + ",underlined:" + underlined
					+ ",strikethrough:" + strikethrough + ",obfuscated:" + obfuscated + ",clickEvent:{action:'" + clickevent + "',value:'" + clickeventvalue + "'}"
					+ ",hoverEvent:{action:'" + hoverevent + "',value:" + hovereventvalue + "}" + ",extra:[";
		}
		public boolean isValid(){
			return (string != null && color != null);
		}
		//字体パラメーター処理
		public JSONPart setParam(JSONParam... params){
			for(JSONParam param : params){
				if(param == JSONParam.BOLD){
					bold = true;
				} else if(param == JSONParam.ITALIC){
					italic = true;
				} else if(param == JSONParam.OBFUSCATED){
					obfuscated = true;
				} else if(param == JSONParam.STRIKETHROUGH){
					strikethrough = true;
				} else if(param == JSONParam.UNDERLINED){
					underlined = true;
				}
			}
			return this;
		}
		//クリック処理
		public JSONPart setClickEvent(JSONClickEvent events , String value) {
			if (value != null) {
				clickeventvalue = value;
			}
			if (events == JSONClickEvent.RUN_COMMAND) {
				clickevent = "run_command";
			} else if (events == JSONClickEvent.SUGGEST_COMMAND) {
				clickevent = "suggest_command";
			} else if (events == JSONClickEvent.OPEN_URL) {
				clickevent = "open_url";
			} else if (events == JSONClickEvent.CHANGE_PAGE) {
				clickevent = "change_page";
			}
			return this;
		}
		//ホバー処理
		public JSONPart setHoverEvent(List<JSONPart> list) {
			hoverevent = "show_text";
			if (list != null) {
				hovereventvalue = JSONString(list);
			}
			return this;
		}
	}

	//ListをString化
	/**
	 * LinkedListをJSONのStringに変換します。
	 *
	 * @param list JSONPartのList
	 */
	public static String JSONString(List<JSONPart> list){
		if(list == null){
			new NullPointerException("The list cannot be null !").printStackTrace();
			return null;
		}
		if(list.size() < 1){
			new IndexOutOfBoundsException("The must contains at least 1 element !").printStackTrace();
			return null;
		}
		if(list.size() > 1){
			String result = "";
			boolean first_done = false;
			for(int i = 0; i < list.size(); i++){
				JSONPart json_part = list.get(i);
				if(!first_done){
					result = json_part.__INVALID__getJSONPartExtra();
					first_done = true;
				} else {
					if(list.size() >= (i+2)){
						result = result + json_part.__INVALID__getJSONPartExtra();
					} else {
						result = result + json_part.getJSONPart();
						for(int end = 0; end < i; end++){
							result = result + "]}";
						}
						return result;
					}
				}
			}
		} else {
			return list.get(0).getJSONPart();
		}
		return null;
	}

}
