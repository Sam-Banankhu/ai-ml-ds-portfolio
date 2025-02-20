import torch
from datasets import load_dataset
from transformers import AutoModelForCausalLM, AutoTokenizer
from tqdm import tqdm

# Configuration
new_model = "Llama-2-7b-public-health-chat-finetune"
dataset_name = "sambanankhu/public-health-QA-handouts-instruct-Llama-2k"

# Load fine-tuned model and tokenizer
model = AutoModelForCausalLM.from_pretrained(new_model)
tokenizer = AutoTokenizer.from_pretrained(new_model)

# Load test dataset
dataset = load_dataset(dataset_name)
train_test_split = dataset["train"].train_test_split(test_size=0.1)
test_dataset = train_test_split["test"]

# Perplexity Calculation
model.eval()
total_loss = 0
total_samples = 0

for example in tqdm(test_dataset):
    inputs = tokenizer(example["text"], return_tensors="pt", truncation=True, padding=True)
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    with torch.no_grad():
        outputs = model(**inputs, labels=inputs["input_ids"])
        loss = outputs.loss
        total_loss += loss.item()
        total_samples += 1

perplexity = torch.exp(torch.tensor(total_loss / total_samples))
print(f"Perplexity: {perplexity}")

# Accuracy Calculation
correct = 0
total = 0

for example in tqdm(test_dataset):
    inputs = tokenizer(example["text"], return_tensors="pt", truncation=True, padding=True)
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    with torch.no_grad():
        outputs = model(**inputs)
        predictions = torch.argmax(outputs.logits, dim=-1)
        correct += (predictions == inputs["input_ids"]).sum().item()
        total += inputs["input_ids"].numel()

accuracy = correct / total
print(f"Accuracy: {accuracy}")

# Save evaluation results
with open("evaluation_results.txt", "w") as f:
    f.write(f"Perplexity: {perplexity}\n")
    f.write(f"Accuracy: {accuracy}\n")