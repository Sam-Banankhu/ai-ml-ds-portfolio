# @router.post("/generate-handout", response_model=HandoutResponse)
# async def generate_handout(
#     query: HandoutQuery,
#     db: Session = Depends(get_db),
#     current_user: Users = Depends(get_current_user)
# ):
#     # Ensure user is an instructor
#     check_is_instructor(current_user)

#     # Fetch the section
#     section = db.query(Section).filter_by(id=query.section_id).first()
#     if not section:
#         raise HTTPException(status_code=404, detail="Section not found")

#     # Generate handout content using LLM and vector store
#     qa = RetrievalQA.from_chain_type(
#         llm=llm,
#         chain_type="stuff",
#         retriever=vector_store.as_retriever(search_kwargs={'k': 5}),
#         chain_type_kwargs={"prompt": PROMPT}
#     )

#     # Invoke LLM to get handout content
#     response = qa.invoke(query.topic)
#     handout_content = response.get('result')
    
#     if not handout_content:
#         raise HTTPException(status_code=500, detail="Failed to generate handout content")

#     # Create and save new handout in the database
#     new_handout = Handout(
#         title=query.topic,
#         content=handout_content,
#         created_by_id=current_user.id,
#         section_id=query.section_id,
#         created_at=datetime.now()
#     )
#     db.add(new_handout)
#     db.commit()
#     db.refresh(new_handout)

#     # Return the newly created handout in the same format as the HandoutResponse model
#     return HandoutResponse(
#         id=new_handout.id,
#         instructor_name=current_user.fullname,
#         title=new_handout.title,
#         content=new_handout.content,
#         section_name=section.name,
#         created_at=new_handout.created_at
#     )