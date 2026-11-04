run:
	@./run.sh

test:
	@mvn test

clean:
	@rm -rf target
	@rm -rf out

wip:
	git add .
	git commit -m "wip"
	git push
