from transformers import AutoModelForCausalLM, AutoTokenizer
from datasets import load_dataset

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

# Generate Predictions
for example in test_dataset:
    inputs = tokenizer(example["text"], return_tensors="pt", truncation=True, padding=True)
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    with torch.no_grad():
        outputs = model.generate(**inputs, max_length=50)
        generated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
        print(f"Generated: {generated_text}")
        print(f"Ground Truth: {example['text']}")
        print("-" * 50)