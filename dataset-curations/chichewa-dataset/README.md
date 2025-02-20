# 📌 **Radio Maria Malawi News Scraper**  

This project is a web scraper designed to extract news articles from the **Radio Maria Malawi** website. The scraper collects article **titles, content, authors, publication dates, and links** and saves them into a CSV file.  

---

## 🛠️ **Project Structure**  

```
📂 project-folder/
│── 📄 scraper.py         # Main script to run the scraper
│── 📄 functions.py       # Helper functions for scraping
│── 📂 assets/            # Additional resources
│── 📄 requirements.txt   # Dependencies
```

---

## 🚀 **How to Run the Scraper**  

### 1️⃣ Install Dependencies  
Ensure you have the required Python libraries installed. Run:  

```bash
pip install pandas requests beautifulsoup4 tqdm
```

### 2️⃣ Run the Scraper  
Execute the main script:  

```bash
python scraper.py
```

---

## 📜 **What the Code Does**  

- **`functions.py`**: Contains helper functions for:  
  - Fetching and parsing web pages (`get_url`)  
  - Extracting article details (`get_links_title`)  
  - Storing data in a structured format (`create_csv`)  

- **`scraper.py`**:  
  - Iterates through **227 pages** of news articles  
  - Calls functions from `functions.py` to extract and process data  
  - Saves the scraped data into `final_radio_maria_chichewa_lang_news.csv`  

---

## 📂 **Output**  
A CSV file named **`final_radio_maria_chichewa_lang_news.csv`** will be generated with the following columns:  

| Title | News Content | Link | Date | Author |  
|-------|-------------|------|------|--------|  
| Sample Title | Full article text... | `https://www.radiomaria.mw/...` | `01-02-2024` | `John Doe` |  

---

## ⚠️ **Notes**  
- Ensure you have a **stable internet connection**.  
- Modify `sleep(0.05)` in `functions.py` to **reduce server load** if needed.  
- If an error occurs, the script **skips the page** and continues.  

---

## 📧 **Contact**  
📌 **Author:** *Samson Mhango*  
🔗 **GitHub:** [sambanankhu](https://github.com/sambanankhu)  

🚀 **Happy Scraping!** 🚀