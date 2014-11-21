/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.util;

import java.text.Normalizer;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

public class StringUtils {
	public static String repeat(String string, int repeat) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < repeat; i++)
			str.append(string);
		return str.toString();
	}

	public static String capitalizeString(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i] == '.'
					|| chars[i] == '\'') {
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	/**
	 * Html to string.
	 * 
	 * @param html
	 *            the html
	 * @return the string
	 */
	public static String htmlToString(String html) {

		return Html.fromHtml(
				html.replaceAll("\\<.*?\\>", "").replaceAll("\n", "")
						.replaceAll("\t", " ")).toString();
	}

	/**
	 * String to html.
	 * 
	 * @param string
	 *            the string
	 * @return the spanned
	 */
	public static Spanned stringToHtml(String string) {
		return Html.fromHtml(string);
	}

	public static CharSequence highlight(int color, String search,
			String originalText) {
		// ignore case and accents
		// the same thing should have been done for the search text
		String normalizedText = Normalizer
				.normalize(originalText, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase();

		int start = normalizedText.indexOf(search);
		if (start < 0) {
			// not found, nothing to to
			return originalText;
		} else {
			// highlight each appearance in the original text
			// while searching in normalized text
			Spannable highlighted = new SpannableString(originalText);
			while (start >= 0) {
				int spanStart = Math.min(start, originalText.length());
				int spanEnd = Math.min(start + search.length(),
						originalText.length());

				highlighted.setSpan(new BackgroundColorSpan(color), spanStart,
						spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				start = normalizedText.indexOf(search, spanEnd);
			}

			return highlighted;
		}
	}
}
