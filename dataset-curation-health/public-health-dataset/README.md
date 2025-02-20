# 📘 Public Health QA Handouts Instruct Llama 2k Dataset  

[![DOI](https://img.shields.io/badge/DOI-10.57967/hf/3034-blue.svg)](https://doi.org/10.57967/hf/3034)  
[![License: AFL-3.0](https://img.shields.io/badge/license-AFL--3.0-blue)](https://opensource.org/licenses/AFL-3.0)  

## 📝 Overview  
The **Public Health QA Handouts Instruct Llama 2k** dataset is designed for fine-tuning **instruction-following language models**, particularly for **public health education** and **disease surveillance response**. The dataset consists of **2,020 structured QA pairs**, extracted from training handouts and technical guidelines relevant to health professionals.  

This dataset provides high-quality, structured **instructional prompts** and responses, making it useful for:  
- **Fine-tuning LLMs** (e.g., Llama 2) on public health topics  
- **Developing question-answering (QA) systems**  
- **Improving disease surveillance automation**  

## 📂 Dataset Details  

- **Dataset Size:** ~816.82k characters  
- **Number of Entries:** **2,020 rows**  
- **Format:** **Text**  
- **Modalities:** Instructional Text (Prompts & Responses)  
- **Split:**  
  - `train`: **2.02k rows**  
- **Libraries Supported:**  
  - 🤗 `datasets`  
  - `pandas`  

## 📑 Structure  
Each data instance consists of a structured **instruction-response** pair formatted for LLM fine-tuning.  

| Column Name   | Description  |  
|--------------|-------------|  
| `text`       | Instructional text input and response in **Llama-style formatting** (`<s>[INST]...[/INST] response </s>`)  |  

### 🔹 Example Entry  
```
<s>[INST] When redistributing or adapting the work from the technical guidelines for integrated disease surveillance and response in the WHO African Region, ensure that it is for non-commercial purposes, appropriately cited, and that there is no suggestion of WHO endorsement. The WHO logo is not permitted for use. If adapting the work, it must be licensed under the same or equivalent Creative Commons licence. If creating a translation, include the disclaimer provided along with the citation. [/INST] 

"This translation was not created by the World Health Organization (WHO). WHO is not responsible for the accuracy or completeness of this translation." When redistributing or adapting the work from the Technical Guidelines for Integrated Disease Surveillance and Response in the WHO African Region, Booklet Two: Sections 1, 2, and 3 (WHO/AF/WHE/CPI/01, 2019), it is important to ensure that it is done for non-commercial purposes. The work is available under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 IGO licence, which allows for copying, redistributing, and adapting the work as long as it is appropriately cited. In using this work, it is crucial to avoid suggesting that WHO endorses any specific organization, products, or services. The use of the WHO logo is not permitted. If the work is adapted, it must be licensed under the same or equivalent Creative Commons licence. If a translation of the work is created, the provided disclaimer must be included along with the citation to clarify that the translation was not created by WHO and that WHO is not responsible for its accuracy or completeness. </s>
```

## 🎯 Usage  
### 🔹 Loading the Dataset  
```python
from datasets import load_dataset

dataset = load_dataset("sambanankhu/public-health-QA-handouts-instruct-Llama-2k")
```

### 🔹 Using with Pandas  
```python
import pandas as pd

df = pd.read_csv("public-health-QA-handouts-instruct-Llama-2k.csv")
print(df.head())
```

### 🔹 Fine-tuning Llama 2  
The dataset is optimized for training **instruction-following LLMs**, making it ideal for models like **Llama 2**, **Mistral**, and **GPT-4**.  

## 🚀 Applications  
- **LLM fine-tuning for health education**  
- **QA systems for public health professionals**  
- **Disease surveillance AI tools**  
- **Instructional chatbot development**  

## 🤝 Contribution  
Feel free to contribute by submitting pull requests or opening issues on the [Hugging Face dataset repository](https://huggingface.co/datasets/sambanankhu/public-health-QA-handouts-instruct-Llama-2k).  

## 📬 Citation  
If you use this dataset in your research or application, please cite:  
```bibtex
@dataset{sambanankhu2024publichealthQA,
  author = {Sambanankhu},
  title = {Public Health QA Handouts Instruct Llama 2k},
  year = {2024},
  doi = {10.57967/hf/3034}
}
```

---  
🔗 **Hugging Face Dataset:** [sambanankhu/public-health-QA-handouts-instruct-Llama-2k](https://huggingface.co/datasets/sambanankhu/public-health-QA-handouts-instruct-Llama-2k)  
📧 **Contact:** [Hugging Face Profile](https://huggingface.co/sambanankhu)