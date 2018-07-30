#!/usr/bin/env bash
REGISTRY="${REGISTRY:-hub.fzyun.io:5000}"

set -e

cd "$(dirname "$(readlink -f "$BASH_SOURCE")")"
repo="$(basename "$PWD")"

projects=( "$@" )
if [ ${#projects[@]} -eq 0 ]; then
    projects=( */ )
fi
projects=( "${projects[@]%/}" )

for project in "${projects[@]}"; do
    cd "$project"
    image="founder/$project:$TAG"
    docker build -t "$image" .
    docker tag "$image" "$REGISTRY/$image"
    docker push "$REGISTRY/$image"
    docker rmi "$REGISTRY/$image"
    cd -
done

