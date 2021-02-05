import requests
import csv342 as csv

import psycopg2
from bs4 import BeautifulSoup

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

import time

# URL = 'https://www.fishbase.de/Country/CountryChecklist.php?showAll=yes&what=list&trpp=50&c_code=643&cpresence=present&sortby=alpha2&vhabitat='
# counter = 0;
# types = ['fresh', 'saltwater']

# for type_ in types:
# 	url = URL + type_
# 	r = requests.get(url)
# 	soup = BeautifulSoup(r.text,'html.parser')
# 	all_strings = soup.find_all('tr', class_ = 't_value1')
# 	counter += len(all_strings)
# 	translator = Translator();
# 	for string_ in all_strings:
# 		all_colums = string_.find_all('td')
# 		if all_colums[5].text.strip():
# 			# src_ = 'ru' if all_colums[5].find('\'') != -1 else 'en'
# 			# res = translator.translate(all_colums[5].text.strip(), dest='ru')
# 			# print(res.text.capitalize())
# 			pass
# 		elif all_colums[4].text.strip():
# 			res = translator.translate(all_colums[4].text.strip(), dest='ru')
# 			print(all_colums[4].text.strip()+ ' - ' + res.text.capitalize())
# print(counter)
def parse():
	con = psycopg2.connect(
		host = "37.230.114.186",
		dbname = "rybalnyadb",
		user = "admin",
		password = "rybalnya2020"
	)
	con.set_client_encoding('UTF8')
	# print(con.encoding)
	cursor = con.cursor()
	try:
		cursor.execute("DELETE FROM aquatic_creatures")
		# print("HERE")
	except Exception as e:
		cursor.execute("ROLLBACK")
		cursor.execute("CREATE TABLE aquatic_creatures (id serial PRIMARY KEY, family text, name text);")
		con.commit()
	
	URL = 'http://www.sevin.ru/vertebrates/index.html?pre_fishes.html'
	options = webdriver.FirefoxOptions()
	options.add_argument('--headless')
	driver = webdriver.Firefox(options=options)
	driver.get(URL)
	driver.switch_to.frame("leftmenuframe")
	driver.switch_to.frame("headframe")
	elements = driver.find_elements_by_css_selector('a[href^="JavaScript:NfindNames"]')
	counter = 0
	for element in elements:
		element.click()
		driver.switch_to.parent_frame()
		driver.switch_to.frame("listframe")
		soup = BeautifulSoup(driver.page_source,'html.parser')
		strings = soup.find_all('table')
		family = ""
		for str_ in strings:
			td_s = str_.find_all('td')
			if len(td_s[0].find_all('img')) == 4 :
				family = td_s[1].text
			if len(td_s[0].find_all('img')) == 5 :
				# print(str(counter) + ' - ' +  family + ' - ' + td_s[1].text.capitalize())
				cursor.execute("INSERT into aquatic_creatures (family, name) values (%s, %s)", (family, td_s[1].get_text(strip=True).capitalize()))
				counter+=1
		driver.switch_to.parent_frame()
		driver.switch_to.frame("headframe")
	con.commit()
	cursor.close()
	con.close()
	driver.quit()
	print(counter + "creatures succsessfull added")

if __name__ == '__main__':
	parse()