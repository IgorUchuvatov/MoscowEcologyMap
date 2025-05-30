import requests
from bs4 import BeautifulSoup
import json

url = "https://kudamoscow.ru/place/park/"
print(f"Запрашиваю URL: {url}")
response = requests.get(url)
print(f"Статус ответа: {response.status_code}")
soup = BeautifulSoup(response.text, "html.parser")

parks = []
for item in soup.find_all("div", class_="place-list-item__title"):
    name = item.get_text(strip=True)
    link = item.find("a")["href"] if item.find("a") else ""
    parks.append({"name": name, "link": link})
    print(f"Найден парк: {name}")

with open("moscow_parks.json", "w", encoding="utf-8") as f:
    json.dump(parks, f, ensure_ascii=False, indent=2)

print(f"Сохранено парков: {len(parks)}") 