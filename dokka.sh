git config --local user.email "action@github.com"
git config --local user.name "GitHub Action"
git fetch origin gh-pages

if [ ! -d "docs" ]; then
  mkdir docs
fi;

cp -Rfv api/target/dokka/* ./docs/

git checkout gh-pages

for dir in ./*
do
  if [ "$dir" == "./docs" ]; then
    continue
  fi

  rm -rf "$dir"
done

cp -Rfv ./docs/* ./
rm -rf ./docs

echo "plasmaenchants.teaminceptus.us" > CNAME

git add .
git branch -D gh-pages
git branch -m gh-pages
git commit -m "Update KDocs ($1)"
git push -f origin gh-pages