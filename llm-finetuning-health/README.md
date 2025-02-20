# README: Fine-Tuning LLaMA-2 on Public Health QA Handouts

This repository provides a setup for fine-tuning the **NousResearch/Llama-2-7b-chat-hf** model using a **public health QA dataset**. The fine-tuned model is optimized for responding to health-related queries based on structured handouts.

---

## 📌 Installation

To set up your environment, install the necessary dependencies:

```bash
pip install -q fsspec==2024.6.1 pyarrow==14.0.2 accelerate==0.21.0 peft==0.4.0 bitsandbytes==0.40.2 transformers==4.31.0 trl==0.4.7
```

Additionally, install or update the **datasets** package:

```bash
pip install -U datasets
```

---

## 📂 Project Overview

### 🏗️ Model & Dataset

- **Base Model**: `NousResearch/Llama-2-7b-chat-hf` (LLaMA-2, 7B parameters)
- **Fine-Tuning Dataset**: `sambanankhu/public-health-QA-handouts-instruct-Llama-2k`

### ⚙️ Quantization & Optimization

- **QLoRA for efficient fine-tuning**
- **4-bit quantization with `bitsandbytes`**
- **LoRA (Low-Rank Adaptation) settings for memory efficiency**

---

## ⚙️ Model Configuration

### 🔹 QLoRA Parameters

| Parameter       | Value |
|----------------|------:|
| `lora_r`      | 64 |
| `lora_alpha`  | 16 |
| `lora_dropout` | 0.1 |

### 🔹 `bitsandbytes` Parameters

| Parameter | Value |
|-----------|------:|
| `use_4bit` | `True` |
| `bnb_4bit_compute_dtype` | `float16` |
| `bnb_4bit_quant_type` | `nf4` |
| `use_nested_quant` | `False` |

---

## 🏋️ Training Setup

### 🔹 Training Arguments

| Parameter | Value |
|-----------|------:|
| `output_dir` | `./results` |
| `num_train_epochs` | `1` |
| `per_device_train_batch_size` | `4` |
| `per_device_eval_batch_size` | `4` |
| `gradient_checkpointing` | `True` |
| `learning_rate` | `2e-4` |
| `weight_decay` | `0.001` |
| `optim` | `"paged_adamw_32bit"` |
| `lr_scheduler_type` | `"cosine"` |
| `warmup_ratio` | `0.03` |
| `group_by_length` | `True` |

---

## 📥 Dataset Loading

The dataset is loaded using the `datasets` library:

```python
from datasets import load_dataset

dataset_name = "sambanankhu/public-health-QA-handouts-instruct-Llama-2k"
dataset = load_dataset(dataset_name, split="train")
```

📌 **Note**: Ensure you have access to the dataset via **Hugging Face Hub**.

---

## 🚀 Running the Training

1. **Ensure dependencies are installed** (as shown in the installation steps).
2. **Authenticate with Hugging Face Hub** (for private datasets/models):
   ```python
   from huggingface_hub import notebook_login
   notebook_login()
   ```
3. **Run the fine-tuning script**.

---

## 📌 Notes & Troubleshooting

- **Hugging Face Authentication**:
  If prompted, create a Hugging Face token from [Hugging Face Tokens](https://huggingface.co/settings/tokens) and authenticate using:
  ```python
  from huggingface_hub import login
  login(token="your_huggingface_token")
  ```
  
- **Dependency Conflicts**:
  If you encounter **pyarrow version conflicts**, manually install a compatible version:
  ```bash
  pip install pyarrow==14.0.2
  ```

- **CUDA Issues**:
  Ensure `torch` is installed with GPU support:
  ```bash
  pip install torch --index-url https://download.pytorch.org/whl/cu118
  ```

---

## 🏁 Conclusion

This setup enables fine-tuning of the **LLaMA-2 7B** model using **QLoRA**, **bitsandbytes**, and **LoRA**, optimized for handling **public health-related queries**. The model can be further trained or evaluated to improve responses and knowledge retrieval efficiency.

For more details on **transformers**, **fine-tuning**, and **dataset preparation**, refer to:
- [Hugging Face Transformers](https://huggingface.co/docs/transformers/index)
- [QLoRA](https://huggingface.co/blog/qlora)

---
📢 **Contributors**: Samson Mhango  
📅 **Last Updated**: February 2025
