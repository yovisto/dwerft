The following commands where used to convert, merge, link, and update the Testdreh Triple Store Data V2:

# Upload Project
java -jar dwerft-tools.jar upload -k testdreh_v2_project testdreh_project_v2.ttl

# Convert DramaQueen and Preproducer
java -jar dwerft-tools.jar convert v2_testdreh_v2.dq v2_testdreh_dq.ttl -u http://filmontology.org/resource/Production/f434d145-f054-40f8-bf61-052eb0e0d691
java -jar dwerft-tools.jar convert v2_testdreh_pp.ttl -u http://filmontology.org/resource/Production/f434d145-f054-40f8-bf61-052eb0e0d691

# Upload DramaQueen and Merge Preproducer
java -jar dwerft-tools.jar upload -k testdreh_v2_dramaqueen_preproducer v2_testdreh_dq.ttl
java -jar dwerft-tools.jar merge -k testdreh_v2_dramaqueen_preproducer v2_testdreh_pp.ttl
java -jar dwerft-tools.jar upload -k testdreh_v2_dramaqueen_preproducer v2_testdreh_pp-merged.ttl

# Convert ALE Silverstack and Lockit
java -jar dwerft-tools.jar convert d-werft_testdreh_silverstack_library.ale v2_testdreh_v2_ale_silver.ttl  -u http://filmontology.org/resource/Production/f434d145-f054-40f8-bf61-052eb0e0d691
java -jar dwerft-tools.jar convert D_2_Cam_1.ale v2_testdreh_v2_ale_lockit.ttl -u http://filmontology.org/resource/Production/f434d145-f054-40f8-bf61-052eb0e0d691

# Upload ALE Silverstack and Merge Lockit
java -jar dwerft-tools.jar upload -k testdreh_v2_ale v2_testdreh_v2_ale_silver.ttl
java -jar dwerft-tools.jar merge -k testdreh_v2_ale v2_testdreh_v2_ale_lockit.ttl
java -jar dwerft-tools.jar upload -k testdreh_v2_ale v2_testdreh_v2_ale_lockit-merged.ttl

# Link ALE with Script and Upload
java -jar dwerft-tools.jar linkale -k testdreh_v2_dramaqueen_preproducer v2_testdreh_v2_ale_lockit-merged.ttl
java -jar dwerft-tools.jar upload -k testdreh_v2_ale v2_testdreh_v2_ale_lockit-merged-linked.ttl


