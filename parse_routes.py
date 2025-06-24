import requests
from bs4 import BeautifulSoup
import json

url = "https://kudago.com/msk/list/ekologicheskie-tropyi-moskvyi/"
print(f"Запрашиваю URL: {url}")
response = requests.get(url)
print(f"Статус ответа: {response.status_code}")
soup = BeautifulSoup(response.text, "html.parser")

routes = []
# Находим все заголовки h3, которые, как ожидается, содержат названия маршрутов
for item in soup.select("h3"):
    # Получаем текст из тега <a> внутри <h3>, если он есть
    a_tag = item.find("a")
    if a_tag:
        name = a_tag.get_text(strip=True)
        # Ищем следующий элемент, который должен быть описанием
        description_tag = item.find_next_sibling("p")
        description = description_tag.get_text(strip=True) if description_tag else ""

        # У KudaGo есть свои карточки, которые тоже попадают в h3, пропускаем их
        if "по версии KudaGo" in name:
            continue

        routes.append({"name": name, "link": a_tag.get('href', ''), "description": description})
        print(f"Найден маршрут: {name}")


with open("ecological_routes.json", "w", encoding="utf-8") as f:
    json.dump(routes, f, ensure_ascii=False, indent=2)

print(f"Сохранено маршрутов: {len(routes)}") 